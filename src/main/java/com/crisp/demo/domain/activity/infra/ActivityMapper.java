package com.crisp.demo.domain.activity.infra;

import com.crisp.demo.domain.activity.model.Activity;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ActivityMapper {

    @Select("SELECT a.*, c.name AS club_name, u.name AS creator_name FROM activity a LEFT JOIN club c ON a.club_id = c.id LEFT JOIN user u ON a.creator_id = u.id WHERE a.id = #{id}")
    Activity findById(Long id);

    @Select("SELECT a.*, c.name AS club_name, u.name AS creator_name FROM activity a LEFT JOIN club c ON a.club_id = c.id LEFT JOIN user u ON a.creator_id = u.id ORDER BY a.created_at DESC")
    List<Activity> findAll();

    @Select("SELECT a.*, c.name AS club_name, u.name AS creator_name FROM activity a LEFT JOIN club c ON a.club_id = c.id LEFT JOIN user u ON a.creator_id = u.id WHERE a.club_id = #{clubId} ORDER BY a.created_at DESC")
    List<Activity> findByClubId(Long clubId);

    @Select("SELECT a.*, c.name AS club_name, u.name AS creator_name FROM activity a LEFT JOIN club c ON a.club_id = c.id LEFT JOIN user u ON a.creator_id = u.id WHERE a.status = #{status} ORDER BY a.start_time ASC")
    List<Activity> findByStatus(String status);

    @Select("SELECT a.*, c.name AS club_name, u.name AS creator_name FROM activity a LEFT JOIN club c ON a.club_id = c.id LEFT JOIN user u ON a.creator_id = u.id WHERE a.title LIKE CONCAT('%', #{keyword}, '%')")
    List<Activity> search(String keyword);

    @Select("SELECT a.*, c.name AS club_name, u.name AS creator_name FROM activity a LEFT JOIN club c ON a.club_id = c.id LEFT JOIN user u ON a.creator_id = u.id WHERE a.creator_id = #{creatorId} ORDER BY a.created_at DESC")
    List<Activity> findByCreatorId(Long creatorId);

    @Insert("INSERT INTO activity(club_id, title, description, location, start_time, end_time, max_participants, current_count, status, join_scope, creator_id) VALUES(#{clubId}, #{title}, #{description}, #{location}, #{startTime}, #{endTime}, #{maxParticipants}, #{currentCount}, #{status}, #{joinScope}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Activity activity);

    @Update("UPDATE activity SET title=#{title}, description=#{description}, location=#{location}, start_time=#{startTime}, end_time=#{endTime}, max_participants=#{maxParticipants}, status=#{status}, join_scope=#{joinScope} WHERE id=#{id}")
    int update(Activity activity);

    @Update("UPDATE activity SET current_count = current_count + 1 WHERE id=#{id} AND (max_participants = 0 OR current_count < max_participants)")
    int incrementCount(Long id);

    @Update("UPDATE activity SET current_count = current_count - 1 WHERE id=#{id} AND current_count > 0")
    int decrementCount(Long id);

    @Update("UPDATE activity SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM activity WHERE id=#{id}")
    int deleteById(Long id);
}