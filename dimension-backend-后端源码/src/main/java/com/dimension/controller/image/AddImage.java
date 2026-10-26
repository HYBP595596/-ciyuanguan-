package com.dimension.controller.image;

import cn.hutool.core.text.StrBuilder;
import cn.hutool.json.JSONUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AddImage", value = "/AddImage")
public class AddImage extends BaseImageServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StrBuilder json = new StrBuilder();
        String jsonLine = "";
        while ((jsonLine = req.getReader().readLine()) != null) {
            json.append(jsonLine);
        }
        Map map = JSONUtil.toBean(json.toString(), Map.class);

        // hutool 会把 JSON 的 null 解析成 JSONNull，直接强转 String 会抛 ClassCastException
        String url = asString(map.get("url"));
        Integer categoryId = (Integer) map.get("categoryValueId");
        Long userId = Long.valueOf(map.get("userId").toString());

        // 多图：第 1 张是 url，其余由前端拼成逗号分隔的 extraImages
        String extraImages = asString(map.get("extraImages"));

        // 小说正文(.txt)的地址，其它类型为空
        String txtUrl = asString(map.get("txtUrl"));

        // 👇 核心修改：不要强转 Integer，直接拿 List<Object>
        List<Object> rawTags = (List<Object>) map.get("tagListValueIds");

        // 调用 Service 时传 List<Object>
        imageService.addImage(url, categoryId, rawTags, userId, extraImages, txtUrl);

        JsonResultOutputUtil.outputJsonResult(resp, cn.hutool.core.map.MapUtil.builder()
                .put("flag", 0)
                .put("message", "发布成功，等待管理员审核")
                .build());
    }

    /** JSON 的 null 会被 hutool 转成 JSONNull，这里统一取成真正的 null / 去空字符串 */
    private static String asString(Object value) {
        if (value == null || value instanceof cn.hutool.json.JSONNull) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }
}