package com.dimension.controller.image;


import cn.hutool.core.text.StrBuilder;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//接口3.12 小鹏
@WebServlet(name = "UpdateImage", value = "/UpdateImage")
public class UpdateImage extends BaseImageServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StrBuilder json = new StrBuilder(); // 字符流中的json
        String jsonLine = "";
        // 一行一行循环读取
        while ((jsonLine = req.getReader().readLine()) != null) {
            json.append(jsonLine);
        }
        Map map = JSONUtil.toBean(json.toString(), Map.class);

        //将参数取出
        Integer categoryId = (Integer)map.get("categoryValueId");
        List<Integer> tagIds = ((JSONArray) map.get("tagListValueIds")).toList(Integer.class);
        Long imageId = Long.parseLong(String.valueOf(map.get("imageId")));

        imageService.updateImageTagsAndCategory(categoryId,tagIds,imageId);
    }
}
