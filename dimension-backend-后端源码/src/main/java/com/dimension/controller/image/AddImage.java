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

        String url = (String) map.get("url");
        Integer categoryId = (Integer) map.get("categoryValueId");
        Long userId = Long.valueOf(map.get("userId").toString());

        // 👇 核心修改：不要强转 Integer，直接拿 List<Object>
        List<Object> rawTags = (List<Object>) map.get("tagListValueIds");

        // 调用 Service 时传 List<Object>
        imageService.addImage(url, categoryId, rawTags, userId);

        JsonResultOutputUtil.outputJsonResult(resp, cn.hutool.core.map.MapUtil.builder()
                .put("flag", 0)
                .put("message", "发布成功，等待管理员审核")
                .build());
    }
}