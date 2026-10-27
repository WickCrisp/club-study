package com.crisp.demo.domain.club.infra;

import com.crisp.demo.domain.club.model.Club;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClubMapper {

    @Select("SELECT c.*, u.name AS leader_name, cc.name AS category_name FROM club c LEFT JOIN user u ON c.leader_id = u.id LEFT JOIN club_category cc ON c.category_id = cc.id WHERE c.id = #{id}")
    Club findById(Long id);

    @Select("SELECT c.*, u.name AS leader_name, cc.name AS category_name FROM club c LEFT JOIN user u ON c.leader_id = u.id LEFT JOIN club_category cc ON c.category_id = cc.id ORDER BY c.created_at DESC")
    List<Club> findAll();

    @Select("SELECT c.*, u.name AS leader_name, cc.name AS category_name FROM club c LEFT JOIN user u ON c.leader_id = u.id LEFT JOIN club_category cc ON c.category_id = cc.id WHERE c.name LIKE CONCAT('%', #{keyword}, '%')")
    List<Club> search(String keyword);

    @Select("SELECT c.*, u.name AS leader_name, cc.name AS category_name FROM club c LEFT JOIN user u ON c.leader_id = u.id LEFT JOIN club_category cc ON c.category_id = cc.id WHERE c.leader_id = #{leaderId} ORDER BY c.created_at DESC")
    List<Club> findByLeaderId(Long leaderId);

    @Insert("INSERT INTO club(name, description, category_id, leader_id, status) VALUES(#{name}, #{description}, #{categoryId}, #{leaderId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Club club);

    @Update("UPDATE club SET name=#{name}, description=#{description}, category_id=#{categoryId}, leader_id=#{leaderId}, status=#{status} WHERE id=#{id}")
    int update(Club club);

    @Update("UPDATE club SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM club WHERE id=#{id}")
    int deleteById(Long id);
}