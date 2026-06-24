package com.crisp.demo.mapper;

import com.crisp.demo.entity.ClubMember;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClubMemberMapper {

    @Select("SELECT cm.*, u.name AS user_name, u.student_no, c.name AS club_name FROM club_member cm LEFT JOIN user u ON cm.user_id = u.id LEFT JOIN club c ON cm.club_id = c.id WHERE cm.id = #{id}")
    ClubMember findById(Long id);

    @Select("SELECT cm.*, u.name AS user_name, u.student_no, c.name AS club_name FROM club_member cm LEFT JOIN user u ON cm.user_id = u.id LEFT JOIN club c ON cm.club_id = c.id WHERE cm.club_id = #{clubId} ORDER BY cm.created_at DESC")
    List<ClubMember> findByClubId(Long clubId);

    @Select("SELECT cm.*, u.name AS user_name, u.student_no, c.name AS club_name FROM club_member cm LEFT JOIN user u ON cm.user_id = u.id LEFT JOIN club c ON cm.club_id = c.id WHERE cm.user_id = #{userId} ORDER BY cm.created_at DESC")
    List<ClubMember> findByUserId(Long userId);

    @Select("SELECT cm.*, u.name AS user_name, u.student_no, c.name AS club_name FROM club_member cm LEFT JOIN user u ON cm.user_id = u.id LEFT JOIN club c ON cm.club_id = c.id WHERE cm.user_id = #{userId} AND cm.club_id = #{clubId} ORDER BY cm.id DESC LIMIT 1")
    ClubMember findByUserAndClub(@Param("userId") Long userId, @Param("clubId") Long clubId);

    @Select("SELECT cm.*, u.name AS user_name, u.student_no, c.name AS club_name FROM club_member cm LEFT JOIN user u ON cm.user_id = u.id LEFT JOIN club c ON cm.club_id = c.id WHERE cm.club_id = #{clubId} AND cm.status = #{status}")
    List<ClubMember> findByClubIdAndStatus(@Param("clubId") Long clubId, @Param("status") String status);

    @Insert("INSERT INTO club_member(user_id, club_id, role, status, apply_reason) VALUES(#{userId}, #{clubId}, #{role}, #{status}, #{applyReason})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ClubMember clubMember);

    @Update("UPDATE club_member SET status=#{status}, role=#{role}, apply_reason=#{applyReason}, joined_at=#{joinedAt}, quit_at=#{quitAt}, created_at=NOW() WHERE id=#{id}")
    int update(ClubMember clubMember);

    @Update("UPDATE club_member SET status='APPROVED', joined_at=NOW() WHERE id=#{id}")
    int approve(Long id);

    @Update("UPDATE club_member SET status='REJECTED' WHERE id=#{id}")
    int reject(Long id);

    @Update("UPDATE club_member SET status='QUIT', quit_at=NOW() WHERE id=#{id}")
    int quit(Long id);

    @Update("UPDATE club_member SET status='QUIT_PENDING' WHERE id=#{id}")
    int requestQuit(Long id);

    @Update("UPDATE club_member SET status='QUIT', quit_at=NOW() WHERE id=#{id}")
    int approveQuit(Long id);

    @Update("UPDATE club_member SET status='APPROVED' WHERE id=#{id}")
    int rejectQuit(Long id);

    @Delete("DELETE FROM club_member WHERE id=#{id}")
    int deleteById(Long id);
}