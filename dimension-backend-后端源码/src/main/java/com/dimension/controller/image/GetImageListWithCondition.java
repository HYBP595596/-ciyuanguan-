package com.dimension.controller.image;


import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;
import com.dimension.vo.ImageTagTZP;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GetImageListWithCondition", value = "/GetImageListWithCondition")
public class GetImageListWithCondition extends BaseImageServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取参数
        String keyword = req.getParameter("keyword");
        Integer pageNum = PageUtil.getPageNum(req);
        Integer pageSize = PageUtil.getPageSize(req);
        //调用service
        List<ImageTagTZP> imageTagList = imageService.getImageTagListWithCondition(keyword,pageNum, pageSize);
        long total = imageService.getTotalWithCondition(keyword);
        //组装数据
        Map<Object, Object> result = MapUtil.builder()
                .put("imageTagList", imageTagList)
                .put("total", total).build();
        //5.输出数据

        JsonResultOutputUtil.outputJsonResult(resp, result);



    }
}
