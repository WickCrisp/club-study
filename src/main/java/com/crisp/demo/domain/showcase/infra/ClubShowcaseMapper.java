package com.crisp.demo.domain.showcase.infra;

import com.crisp.demo.domain.showcase.model.ClubShowcase;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClubShowcaseMapper {

    String BASE = "SELECT s.*, c.name AS club_name, u.name AS creator_name FROM club_showcase s " +
            "LEFT JOIN club c ON s.club_id = c.id LEFT JOIN user u ON s.creator_id = u.id ";

    @Select(BASE + "WHERE s.id = #{id}")
    ClubShowcase findById(Long id);

    /** 某社团的风采条目（聚合页） */
    @Select(BASE + "WHERE s.club_id = #{clubId} AND s.status = 'PUBLISHED' ORDER BY s.sort ASC, s.id DESC")
    List<ClubShowcase> findByClubId(Long clubId);

    /** 某社团的全部条目（管理端，含草稿） */
    @Select(BASE + "WHERE s.club_id = #{clubId} ORDER BY s.sort ASC, s.id DESC")
    List<ClubShowcase> findAllByClubId(Long clubId);

    /** 首页：全部已发布风采 */
    @Select(BASE + "WHERE s.status = 'PUBLISHED' ORDER BY s.sort ASC, s.id DESC")
    List<ClubShowcase> findAllPublished();

    @Insert("INSERT INTO club_showcase(club_id, title, content, cover_url, images, sort, status, creator_id) " +
            "VALUES(#{clubId}, #{title}, #{content}, #{coverUrl}, #{images}, #{sort}, #{status}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ClubShowcase showcase);

    @Update("UPDATE club_showcase SET title=#{title}, content=#{content}, cover_url=#{coverUrl}, " +
            "images=#{images}, sort=#{sort}, status=#{status} WHERE id=#{id}")
    int update(ClubShowcase showcase);

    @Delete("DELETE FROM club_showcase WHERE id=#{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM club_showcase WHERE club_id = #{clubId} AND status = 'PUBLISHED'")
    int countPublishedByClub(Long clubId);

    @Select("SELECT COUNT(*) FROM club_showcase WHERE status = 'PUBLISHED'")
    int countPublished();
}
