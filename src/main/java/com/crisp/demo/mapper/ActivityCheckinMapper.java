package com.crisp.demo.mapper;

import com.crisp.demo.entity.ActivityCheckin;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ActivityCheckinMapper {

    @Select("SELECT ac.*, u.name AS user_name, u.student_no, a.title AS activity_title FROM activity_checkin ac LEFT JOIN user u ON ac.user_id = u.id LEFT JOIN activity a ON ac.activity_id = a.id WHERE ac.id = #{id}")
    ActivityCheckin findById(Long id);

    @Select("SELECT ac.*, u.name AS user_name, u.student_no, a.title AS activity_title FROM activity_checkin ac LEFT JOIN user u ON ac.user_id = u.id LEFT JOIN activity a ON ac.activity_id = a.id WHERE ac.activity_id = #{activityId} ORDER BY ac.created_at DESC")
    List<ActivityCheckin> findByActivityId(Long activityId);

    @Select("SELECT ac.*, u.name AS user_name, u.student_no, a.title AS activity_title FROM activity_checkin ac LEFT JOIN user u ON ac.user_id = u.id LEFT JOIN activity a ON ac.activity_id = a.id WHERE ac.user_id = #{userId} ORDER BY ac.created_at DESC")
    List<ActivityCheckin> findByUserId(Long userId);

    @Select("SELECT ac.*, u.name AS user_name, u.student_no, a.title AS activity_title FROM activity_checkin ac LEFT JOIN user u ON ac.user_id = u.id LEFT JOIN activity a ON ac.activity_id = a.id WHERE ac.registration_id = #{registrationId}")
    ActivityCheckin findByRegistrationId(Long registrationId);

    @Insert("INSERT INTO activity_checkin(registration_id, activity_id, user_id, checkin_status, checkout_status) VALUES(#{registrationId}, #{activityId}, #{userId}, #{checkinStatus}, #{checkoutStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ActivityCheckin checkin);

    @Update("UPDATE activity_checkin SET checkin_time=NOW(), checkin_status='CHECKED' WHERE id=#{id}")
    int checkin(Long id);

    @Update("UPDATE activity_checkin SET checkout_time=NOW(), checkout_status='CHECKED' WHERE id=#{id}")
    int checkout(Long id);

    @Delete("DELETE FROM activity_checkin WHERE id=#{id}")
    int deleteById(Long id);
}