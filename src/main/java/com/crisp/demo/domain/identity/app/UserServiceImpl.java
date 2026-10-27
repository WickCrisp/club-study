package com.crisp.demo.domain.identity.app;

import com.crisp.demo.domain.club.model.Club;
import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.domain.club.api.ClubQueryService;
import com.crisp.demo.domain.identity.infra.UserMapper;
import com.crisp.demo.domain.identity.app.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import com.crisp.demo.shared.error.BusinessException;
import com.crisp.demo.shared.event.DomainEventPublisher;
import com.crisp.demo.shared.event.UserDeleted;
import com.crisp.demo.shared.security.PasswordService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ClubQueryService clubQueryService;
    private final DomainEventPublisher eventPublisher;
    private final PasswordService passwordService;

    @Override
    public User login(String studentNo, String password) {
        User user = userMapper.findByStudentNo(studentNo);
        // 账号不存在与密码错误返回同一句话：否则等于告诉别人哪些学号注册过
        if (user == null || !passwordService.matches(password, user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        // 历史 MD5 密码在登录成功后就地升级为 BCrypt，用户无感
        if (passwordService.needsUpgrade(user.getPassword())) {
            userMapper.updatePassword(user.getId(), passwordService.encode(password));
        }
        return user;
    }

    @Override
    public User register(User user) {
        User existUser = userMapper.findByStudentNo(user.getStudentNo());
        if (existUser != null) {
            throw new BusinessException("该学号已被注册");
        }
        user.setPassword(passwordService.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("STUDENT");
        }
        userMapper.insert(user);
        return user;
    }

    @Override
    public User findById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Override
    public User findByStudentNo(String studentNo) {
        return userMapper.findByStudentNo(studentNo);
    }

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public List<User> search(String keyword) {
        return userMapper.search(keyword);
    }

    @Override
    public boolean update(User user) {
        return userMapper.update(user) > 0;
    }

    @Override
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordService.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        return userMapper.updatePassword(id, passwordService.encode(newPassword)) > 0;
    }

    /**
     * 删除用户
     *
     * 规则：
     * 1) 该用户若是某些社团的负责人，必须先转让负责人再删除，避免产生无主社团（leader_id 被 SET NULL）
     * 2) 删除会级联删除该用户创建的社团成员、报名、活动 —— 本域不清别人的缓存，
     *    而是发布 UserDeleted 事件，由 club / activity 各自清理自己的缓存
     */
    @Override
    public boolean deleteById(Long id) {
        List<Club> ledClubs = clubQueryService.findByLeaderId(id);
        if (!ledClubs.isEmpty()) {
            throw new BusinessException("该用户是 " + ledClubs.size() + " 个社团的负责人，请先转让负责人后再删除");
        }
        boolean deleted = userMapper.deleteById(id) > 0;
        if (deleted) {
            eventPublisher.publish(new UserDeleted(id));
        }
        return deleted;
    }
}
