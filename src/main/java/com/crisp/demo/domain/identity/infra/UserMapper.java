package com.crisp.demo.domain.identity.infra;

import com.crisp.demo.domain.identity.model.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE student_no = #{studentNo}")
    User findByStudentNo(String studentNo);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM user ORDER BY created_at DESC")
    List<User> findAll();

    @Select("SELECT * FROM user WHERE name LIKE CONCAT('%', #{keyword}, '%') OR student_no LIKE CONCAT('%', #{keyword}, '%')")
    List<User> search(String keyword);

    @Insert("INSERT INTO user(student_no, password, name, phone, email, gender, role) VALUES(#{studentNo}, #{password}, #{name}, #{phone}, #{email}, #{gender}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE user SET name=#{name}, phone=#{phone}, email=#{email}, gender=#{gender} WHERE id=#{id}")
    int update(User user);

    @Update("UPDATE user SET password=#{password} WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Delete("DELETE FROM user WHERE id=#{id}")
    int deleteById(Long id);
}