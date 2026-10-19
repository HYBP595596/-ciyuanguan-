package com.dimension.controller.image;

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

//接口3.10 小鹏
@WebServlet(name = "GetImageTableWithNoCondition", value = "/GetImageTableWithNoCondition")
public class GetImageTableWithNoCondition extends BaseImageServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Integer pageNum = PageUtil.getPageNum(req);
        Integer pageSize = PageUtil.getPageSize(req);

        //调用service层获取数据
        List<ImageTagCategory> imageTagCategoryList = imageService.getImageTagCategoryList(pageNum,pageSize);
        long total = imageService.getTotalWithoutCondition();

        //组装数据
        Map<Object,Object> result = MapUtil.builder()
                .put("imageTagCategoryList",imageTagCategoryList)
                .put("total",total).build();

        JsonResultOutputUtil.outputJsonResult(resp, result);

    }
}
