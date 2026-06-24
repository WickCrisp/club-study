package com.crisp.demo.mapper;

import com.crisp.demo.entity.ActivityRegistration;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ActivityRegistrationMapper {

    @Select("SELECT ar.*, u.name AS user_name, u.student_no, a.title AS activity_title FROM activity_registration ar LEFT JOIN user u ON ar.user_id = u.id LEFT JOIN activity a ON ar.activity_id = a.id WHERE ar.id = #{id}")
    ActivityRegistration findById(Long id);

    @Select("SELECT ar.*, u.name AS user_name, u.student_no, a.title AS activity_title FROM activity_registration ar LEFT JOIN user u ON ar.user_id = u.id LEFT JOIN activity a ON ar.activity_id = a.id WHERE ar.activity_id = #{activityId} ORDER BY ar.registered_at DESC")
    List<ActivityRegistration> findByActivityId(Long activityId);

    @Select("SELECT ar.*, u.name AS user_name, u.student_no, a.title AS activity_title FROM activity_registration ar LEFT JOIN user u ON ar.user_id = u.id LEFT JOIN activity a ON ar.activity_id = a.id WHERE ar.user_id = #{userId} ORDER BY ar.registered_at DESC")
    List<ActivityRegistration> findByUserId(Long userId);

    @Select("SELECT ar.*, u.name AS user_name, u.student_no, a.title AS activity_title FROM activity_registration ar LEFT JOIN user u ON ar.user_id = u.id LEFT JOIN activity a ON ar.activity_id = a.id WHERE ar.user_id = #{userId} AND ar.activity_id = #{activityId} ORDER BY ar.id DESC LIMIT 1")
    ActivityRegistration findByUserAndActivity(@Param("userId") Long userId, @Param("activityId") Long activityId);

    @Insert("INSERT INTO activity_registration(activity_id, user_id, status) VALUES(#{activityId}, #{userId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ActivityRegistration registration);

    @Update("UPDATE activity_registration SET status=#{status}, cancelled_at=#{cancelledAt} WHERE id=#{id}")
    int update(ActivityRegistration registration);

    @Update("UPDATE activity_registration SET status='CANCELLED', cancelled_at=NOW() WHERE id=#{id}")
    int cancel(Long id);

    @Update("UPDATE activity_registration SET status='CANCEL_PENDING' WHERE id=#{id}")
    int requestCancel(Long id);

    @Update("UPDATE activity_registration SET status='CANCELLED', cancelled_at=NOW() WHERE id=#{id}")
    int approveCancel(Long id);

    @Update("UPDATE activity_registration SET status='REGISTERED' WHERE id=#{id}")
    int rejectCancel(Long id);

    @Select("SELECT ar.*, u.name AS user_name, u.student_no, a.title AS activity_title FROM activity_registration ar LEFT JOIN user u ON ar.user_id = u.id LEFT JOIN activity a ON ar.activity_id = a.id WHERE ar.status = #{status} ORDER BY ar.registered_at DESC")
    List<ActivityRegistration> findByStatus(String status);

    @Delete("DELETE FROM activity_registration WHERE id=#{id}")
    int deleteById(Long id);
}