package com.crisp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    @NotBlank(message = "学号不能为空")                    // 登录账号，唯一标识
    @Size(min = 4, max = 20, message = "学号长度4-20位")
    private String studentNo;

    @NotBlank(message = "密码不能为空")                    // 注册/登录时需要，更新时可传空
    @Size(min = 6, max = 100, message = "密码长度6-100位")
    private String password;

    @NotBlank(message = "姓名不能为空")                    // 真实姓名
    @Size(max = 50, message = "姓名长度不超过50字")
    private String name;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") // 11位手机号
    private String phone;

    @Email(message = "邮箱格式不正确")                    // 电子邮箱
    private String email;
    private Integer gender;                            // 性别：0未知 1男 2女
    private String role;                               // 角色：STUDENT / ADMIN
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}