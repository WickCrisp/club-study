package com.crisp.demo.mapper;

import com.crisp.demo.entity.ActivitySummary;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ActivitySummaryMapper {

    @Select("SELECT asm.*, a.title AS activity_title, u.name AS creator_name FROM activity_summary asm LEFT JOIN activity a ON asm.activity_id = a.id LEFT JOIN user u ON asm.creator_id = u.id WHERE asm.id = #{id}")
    ActivitySummary findById(Long id);

    @Select("SELECT asm.*, a.title AS activity_title, u.name AS creator_name FROM activity_summary asm LEFT JOIN activity a ON asm.activity_id = a.id LEFT JOIN user u ON asm.creator_id = u.id WHERE asm.activity_id = #{activityId}")
    ActivitySummary findByActivityId(Long activityId);

    @Select("SELECT asm.*, a.title AS activity_title, u.name AS creator_name FROM activity_summary asm LEFT JOIN activity a ON asm.activity_id = a.id LEFT JOIN user u ON asm.creator_id = u.id ORDER BY asm.created_at DESC")
    List<ActivitySummary> findAll();

    @Insert("INSERT INTO activity_summary(activity_id, content, effect, suggestions, images, creator_id) VALUES(#{activityId}, #{content}, #{effect}, #{suggestions}, #{images}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ActivitySummary summary);

    @Update("UPDATE activity_summary SET content=#{content}, effect=#{effect}, suggestions=#{suggestions}, images=#{images} WHERE id=#{id}")
    int update(ActivitySummary summary);

    @Delete("DELETE FROM activity_summary WHERE id=#{id}")
    int deleteById(Long id);
}