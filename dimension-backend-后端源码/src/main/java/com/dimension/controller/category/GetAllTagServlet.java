package com.dimension.controller.category;


import cn.hutool.core.map.MapUtil;
import com.dimension.entity.Tag;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GetAllTag", value = "/GetAllTag")
public class GetAllTagServlet extends BaseTagServlet{
    //7.1 /GetAllTag => 获取所有标签

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //调用service获取数据
        //获取所有标签
        List<Tag> list = tagService.getAllTag();

        //组装数据
        Map<Object,Object> tagList = MapUtil.builder().put("tagList",list).build();

        //响应给前端
        JsonResultOutputUtil.outputJsonResult(resp,tagList);



    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
