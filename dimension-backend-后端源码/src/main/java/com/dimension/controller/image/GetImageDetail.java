package com.dimension.controller.image;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.vo.ImageTagTZP;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GetImageDetail", value = "/GetImageDetail")
public class GetImageDetail extends BaseImageServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long imageId = Long.parseLong(req.getParameter("imageId"));

        ImageTagTZP imageTag=  imageService.GetImageDetailById(imageId);

//        JsonResultOutputUtil.outputJsonResult(resp,imageTag);
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("imageTag", imageTag).build());
    }
}
