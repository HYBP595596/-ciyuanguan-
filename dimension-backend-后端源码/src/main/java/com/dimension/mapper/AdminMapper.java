package com.dimension.mapper;

import com.dimension.entity.Admin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminMapper {
    Admin adminLogin(@Param("username") String username,
                     @Param("password") String password);

}
