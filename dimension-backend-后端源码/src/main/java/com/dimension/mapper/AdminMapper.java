package com.dimension.mapper;

import com.dimension.entity.Admin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminMapper {

    Admin getAdminByUsername(@Param("username") String username);

    Admin getAdminById(@Param("id") Integer id);

    List<Admin> getAdminList(@Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    long getAdminTotal();

    long countAdmins();

    int insertAdmin(Admin admin);

    int updateAdminInfo(@Param("id") Integer id,
                        @Param("username") String username,
                        @Param("nickname") String nickname);

    int updateAdminPassword(@Param("id") Integer id, @Param("password") String password);

    Integer getTokenVersion(@Param("id") Integer id);

    /** 改密码后 +1，让旧 token 失效 */
    int bumpTokenVersion(@Param("id") Integer id);

    int deleteAdmin(@Param("id") Integer id);
}
