package com.dimension.controller.category;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.ImageCategory;
import com.dimension.service.ImageCategoryService;
import com.dimension.service.impl.ImageCategoryServiceImpl;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@WebServlet(name = "GetAllCategory", value = "/GetAllCategory")
public class GetAllTagImageCategoryServlet extends BaseImageCategoryServlet{
    //5.1 /GetAllCategory => 获取所有分类

    protected ImageCategoryService imageCategoryService = new ImageCategoryServiceImpl();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //调用service获取数据
        //查询所有分类,升序记录
        List<ImageCategory> list = imageCategoryService.GetAllCategory();

        //组装数据
        Map<Object,Object> categoryList = MapUtil.builder().put("categoryList",list).build();

        //响应给前端
        JsonResultOutputUtil.outputJsonResult(resp,categoryList);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
