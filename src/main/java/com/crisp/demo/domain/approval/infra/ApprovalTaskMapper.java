package com.crisp.demo.domain.approval.infra;

import com.crisp.demo.domain.approval.model.ApprovalStatus;
import com.crisp.demo.domain.approval.model.ApprovalTask;
import com.crisp.demo.domain.approval.model.ApprovalType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ApprovalTaskMapper {

    String BASE_COLUMNS =
            "SELECT t.*, u.name AS applicant_name, u.student_no AS applicant_student_no " +
            "FROM approval_task t LEFT JOIN user u ON t.applicant_id = u.id ";

    @Select(BASE_COLUMNS + "WHERE t.id = #{id}")
    ApprovalTask findById(Long id);

    /** 某个目标对象上仍在等待处理的待办（同类型同目标只允许一条 PENDING） */
    @Select(BASE_COLUMNS + "WHERE t.type = #{type} AND t.target_id = #{targetId} AND t.status = 'PENDING' ORDER BY t.id DESC LIMIT 1")
    ApprovalTask findPendingByTarget(@Param("type") ApprovalType type, @Param("targetId") Long targetId);

    /** 管理员待办：审批范围为 ADMIN */
    @Select(BASE_COLUMNS + "WHERE t.status = 'PENDING' AND t.approver_scope = 'ADMIN' ORDER BY t.created_at ASC")
    List<ApprovalTask> findPendingForAdmin();

    /** 社团负责人待办：审批范围为该用户 */
    @Select(BASE_COLUMNS + "WHERE t.status = 'PENDING' AND t.approver_scope = 'USER' AND t.approver_id = #{approverId} ORDER BY t.created_at ASC")
    List<ApprovalTask> findPendingForApprover(Long approverId);

    /** 我发起的申请 */
    @Select(BASE_COLUMNS + "WHERE t.applicant_id = #{applicantId} ORDER BY t.created_at DESC")
    List<ApprovalTask> findByApplicant(Long applicantId);

    /** 按状态列全部（管理员明细视图） */
    @Select(BASE_COLUMNS + "WHERE t.status = #{status} ORDER BY t.created_at ASC")
    List<ApprovalTask> findByStatus(@Param("status") String status);

    @Insert("INSERT INTO approval_task(type, target_type, target_id, club_id, applicant_id, approver_scope, approver_id, title, summary, status) " +
            "VALUES(#{type}, #{targetType}, #{targetId}, #{clubId}, #{applicantId}, #{approverScope}, #{approverId}, #{title}, #{summary}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ApprovalTask task);

    /** 决策：只有 PENDING 能被决策，返回 0 表示已被处理（幂等保护） */
    @Update("UPDATE approval_task SET status = #{status}, decided_by = #{decidedBy}, decided_at = NOW(), remark = #{remark} " +
            "WHERE id = #{id} AND status = 'PENDING'")
    int decide(@Param("id") Long id, @Param("status") ApprovalStatus status,
               @Param("decidedBy") Long decidedBy, @Param("remark") String remark);

    /** 目标对象被删除 / 流程被撤销时关闭待办 */
    @Update("UPDATE approval_task SET status = 'CANCELLED', remark = #{remark}, decided_at = NOW() " +
            "WHERE type = #{type} AND target_id = #{targetId} AND status = 'PENDING'")
    int cancelByTarget(@Param("type") ApprovalType type, @Param("targetId") Long targetId, @Param("remark") String remark);

/** 社团相关待办整体关闭 */
    @Update("UPDATE approval_task SET status = 'CANCELLED', remark = #{remark}, decided_at = NOW() " +
            "WHERE club_id = #{clubId} AND status = 'PENDING'")
    int cancelByClub(@Param("clubId") Long clubId, @Param("remark") String remark);

    @Select("SELECT COUNT(*) FROM approval_task WHERE status = 'PENDING'")
    int countPendingAll();

    @Select("SELECT COUNT(*) FROM approval_task WHERE status = 'PENDING' AND approver_scope = 'ADMIN'")
    int countPendingForAdmin();

    @Select("SELECT COUNT(*) FROM approval_task WHERE status = 'PENDING' AND approver_scope = 'USER' AND approver_id = #{approverId}")
    int countPendingForApprover(Long approverId);
}
