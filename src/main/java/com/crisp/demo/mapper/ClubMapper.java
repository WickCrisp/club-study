package com.crisp.demo.mapper;

import com.crisp.demo.entity.Club;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClubMapper {

    @Select("SELECT c.*, u.name AS leader_name FROM club c LEFT JOIN user u ON c.leader_id = u.id WHERE c.id = #{id}")
    Club findById(Long id);

    @Select("SELECT c.*, u.name AS leader_name FROM club c LEFT JOIN user u ON c.leader_id = u.id ORDER BY c.created_at DESC")
    List<Club> findAll();

    @Select("SELECT c.*, u.name AS leader_name FROM club c LEFT JOIN user u ON c.leader_id = u.id WHERE c.name LIKE CONCAT('%', #{keyword}, '%')")
    List<Club> search(String keyword);

    @Select("SELECT c.*, u.name AS leader_name FROM club c LEFT JOIN user u ON c.leader_id = u.id WHERE c.leader_id = #{leaderId} ORDER BY c.created_at DESC")
    List<Club> findByLeaderId(Long leaderId);

    @Insert("INSERT INTO club(name, description, leader_id, status) VALUES(#{name}, #{description}, #{leaderId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Club club);

    @Update("UPDATE club SET name=#{name}, description=#{description}, leader_id=#{leaderId}, status=#{status} WHERE id=#{id}")
    int update(Club club);

    @Delete("DELETE FROM club WHERE id=#{id}")
    int deleteById(Long id);
}