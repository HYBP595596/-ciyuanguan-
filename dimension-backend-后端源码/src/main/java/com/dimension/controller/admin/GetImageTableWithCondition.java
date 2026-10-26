package com.dimension.controller.admin;

import com.dimension.controller.image.BaseImageServlet;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;
import com.dimension.vo.ImageTagCategory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

//接口3.11 小鹏
@WebServlet(name = "GetImageTableWithCondition", value = "/GetImageTableWithCondition")
public class GetImageTableWithCondition extends BaseImageServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取参数
        Integer pageNum = PageUtil.getPageNum(req);
        Integer pageSize = PageUtil.getPageSize(req);
        String keyword = req.getParameter("keyword");

        //调用service层获取数据
        List<ImageTagCategory> imageTagCategoryList = imageService.getImageTagCategoryListByKeyword(keyword,pageNum,pageSize);
        long total = imageService.getTotalByKeyword(keyword);

        //组装数据
        Map<Object,Object> result = MapUtil.builder()
                .put("imageTagCategoryList",imageTagCategoryList)
                .put("total",total).build();

        JsonResultOutputUtil.outputJsonResult(resp, result);


    }
}
