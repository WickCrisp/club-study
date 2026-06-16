package com.crisp.demo.service;

import com.crisp.demo.entity.User;
import java.util.List;

public interface UserService {
    User login(String studentNo, String password);
    User register(User user);
    User findById(Long id);
    User findByStudentNo(String studentNo);
    List<User> findAll();
    List<User> search(String keyword);
    boolean update(User user);
    boolean updatePassword(Long id, String oldPassword, String newPassword);
    boolean deleteById(Long id);
}