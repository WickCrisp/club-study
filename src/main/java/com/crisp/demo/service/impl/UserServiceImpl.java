package com.crisp.demo.service.impl;

import com.crisp.demo.entity.User;
import com.crisp.demo.mapper.UserMapper;
import com.crisp.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.crisp.demo.common.BusinessException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User login(String studentNo, String password) {
        User user = userMapper.findByStudentNo(studentNo);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        return user;
    }

    @Override
    public User register(User user) {
        User existUser = userMapper.findByStudentNo(user.getStudentNo());
        if (existUser != null) {
            throw new BusinessException("该学号已被注册");
        }
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8)));
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
        String encryptedOld = DigestUtils.md5DigestAsHex(oldPassword.getBytes(StandardCharsets.UTF_8));
        if (!encryptedOld.equals(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        String encryptedNew = DigestUtils.md5DigestAsHex(newPassword.getBytes(StandardCharsets.UTF_8));
        return userMapper.updatePassword(id, encryptedNew) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return userMapper.deleteById(id) > 0;
    }
}