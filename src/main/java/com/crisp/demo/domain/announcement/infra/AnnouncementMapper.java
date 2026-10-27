package com.crisp.demo.domain.announcement.infra;

import com.crisp.demo.domain.announcement.model.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    String BASE = "SELECT a.*, u.name AS publisher_name FROM announcement a LEFT JOIN user u ON a.publisher_id = u.id ";

    @Select(BASE + "WHERE a.id = #{id}")
    Announcement findById(Long id);

    /** 已发布公告：置顶优先，其次按发布时间倒序 */
    @Select(BASE + "WHERE a.status = 'PUBLISHED' ORDER BY a.pinned DESC, a.published_at DESC, a.id DESC")
    List<Announcement> findPublished();

    /** 管理端：全部公告 */
    @Select(BASE + "ORDER BY a.pinned DESC, a.id DESC")
    List<Announcement> findAll();

    @Insert("INSERT INTO announcement(title, content, pinned, status, publisher_id, published_at) " +
            "VALUES(#{title}, #{content}, #{pinned}, #{status}, #{publisherId}, #{publishedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement announcement);

    @Update("UPDATE announcement SET title=#{title}, content=#{content}, pinned=#{pinned}, status=#{status}, " +
            "published_at=#{publishedAt} WHERE id=#{id}")
    int update(Announcement announcement);

    @Delete("DELETE FROM announcement WHERE id=#{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM announcement WHERE status = 'PUBLISHED'")
    int countPublished();
}
