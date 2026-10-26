package com.dimension.controller.image;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.Image;
import com.dimension.utils.AdminAuthUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 删除作品。
 * 权限：作者本人（userId），或携带有效管理员 token 的管理员；两者都不满足则拒绝。
 * 不再信任前端传的 adminId 参数。
 */
@WebServlet(name = "RemoveImageById", value = "/RemoveImageById")
public class RemoveImageById extends BaseImageServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long imageId = Long.parseLong(req.getParameter("imageId"));
        Long userId = toLong(req.getParameter("userId"));

        Image image = imageService.getImageById(imageId);
        if (image == null) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", "作品不存在").build());
            return;
        }
        boolean isOwner = userId != null && userId.equals(image.getUserId());
        if (!isOwner && AdminAuthUtil.resolve(AdminAuthUtil.extractToken(req)) == null) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", "没有权限删除该作品").build());
            return;
        }

        imageService.removeImageById(imageId);
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("flag", 0).put("message", "已删除").build());
    }

    private Long toLong(String v) {
        if (v == null || v.trim().isEmpty()) return null;
        try {
            return Long.valueOf(v.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
