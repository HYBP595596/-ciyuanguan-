package com.dimension.controller.image;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.Image;
import com.dimension.entity.Tag;
import com.dimension.mapper.ImageMapper;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.SessionUtil;
import com.dimension.vo.ImageTagTZP;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "GetUserImageList", value = "/GetUserImageList")
public class GetUserImageListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = Long.valueOf(req.getParameter("userId"));

        ImageMapper imageMapper = SessionUtil.getMapper(ImageMapper.class);

        // 1. 查出该用户的所有图片（包括待审核和被驳回的）
        List<Image> imageList = imageMapper.getUserImageList(userId);
        List<ImageTagTZP> resultList = new ArrayList<>();

        // 2. 封装对应的标签
        for (Image image : imageList) {
            List<Tag> tagList = imageMapper.getTagListByImageId(image.getId());
            ImageTagTZP vo = new ImageTagTZP();
            vo.setImage(image);
            vo.setTagList(tagList);
            resultList.add(vo);
        }

        // 3. 返回给前端
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("flag", 0)
                .put("imageList", resultList)
                .build());
    }
}