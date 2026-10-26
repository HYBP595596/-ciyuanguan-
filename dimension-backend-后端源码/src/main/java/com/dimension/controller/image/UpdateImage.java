package com.dimension.controller.image;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.dimension.entity.Image;
import com.dimension.utils.AdminAuthUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 修改作品信息（分类 + 标签）。
 * 权限：必须是作品作者（userId），或管理员（adminId）；两者都不满足则拒绝。
 */
@WebServlet(name = "UpdateImage", value = "/UpdateImage")
public class UpdateImage extends BaseImageServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StrBuilder json = new StrBuilder();
        String jsonLine = "";
        while ((jsonLine = req.getReader().readLine()) != null) {
            json.append(jsonLine);
        }
        Map map = JSONUtil.toBean(json.toString(), Map.class);

        Integer categoryId = (Integer) map.get("categoryValueId");
        Object tagObj = map.get("tagListValueIds");
        List<Integer> tagIds = (tagObj == null || tagObj instanceof cn.hutool.json.JSONNull)
                ? java.util.Collections.emptyList()
                : ((JSONArray) tagObj).toList(Integer.class);
        Long imageId = Long.parseLong(String.valueOf(map.get("imageId")));
        Long userId = toLong(map.get("userId"));

        Image image = imageService.getImageById(imageId);
        if (image == null) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", "作品不存在").build());
            return;
        }
        // 作者本人可以改；否则必须有有效的管理员 token（不再信任前端传的 adminId）
        boolean isOwner = userId != null && userId.equals(image.getUserId());
        if (!isOwner && AdminAuthUtil.resolve(AdminAuthUtil.extractToken(req)) == null) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", "没有权限修改该作品").build());
            return;
        }

        imageService.updateImageTagsAndCategory(categoryId, tagIds, imageId);
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("flag", 0).put("message", "已保存").build());
    }

    private Long toLong(Object v) {
        if (v == null || v instanceof cn.hutool.json.JSONNull) return null;
        try {
            return Long.valueOf(String.valueOf(v).trim());
        } catch (Exception e) {
            return null;
        }
    }
}
