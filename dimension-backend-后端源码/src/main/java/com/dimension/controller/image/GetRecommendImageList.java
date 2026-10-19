package com.dimension.controller.image;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.vo.ImageTagTZP;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetRecommendImageList", value = "/GetRecommendImageList")
public class GetRecommendImageList extends BaseImageServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer categoryId = Integer.parseInt(req.getParameter("categoryId"));

        // 还有图片id - 推荐图片不能查出本图片
        long imageId = Long.parseLong(req.getParameter("imageId"));

        List<ImageTagTZP> imageTagList = imageService.getRecommendImageList(categoryId, imageId);

//        JsonResultOutputUtil.outputJsonResult(resp, imageTagList);
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("imageTagList", imageTagList).build());
    }
}
