package com.crisp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String studentNo;
    private String password;
    private String name;
    private String phone;
    private String email;
    private Integer gender;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}