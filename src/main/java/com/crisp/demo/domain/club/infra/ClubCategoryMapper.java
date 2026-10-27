package com.crisp.demo.domain.club.infra;

import com.crisp.demo.domain.club.model.ClubCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClubCategoryMapper {

    @Select("SELECT * FROM club_category WHERE id = #{id}")
    ClubCategory findById(Long id);

    @Select("SELECT * FROM club_category ORDER BY sort ASC, id ASC")
    List<ClubCategory> findAll();

    @Select("SELECT * FROM club_category WHERE status = 'ACTIVE' ORDER BY sort ASC, id ASC")
    List<ClubCategory> findActive();

    @Insert("INSERT INTO club_category(name, sort, status) VALUES(#{name}, #{sort}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ClubCategory category);

    @Update("UPDATE club_category SET name=#{name}, sort=#{sort}, status=#{status} WHERE id=#{id}")
    int update(ClubCategory category);

    @Delete("DELETE FROM club_category WHERE id=#{id}")
    int deleteById(Long id);
}
