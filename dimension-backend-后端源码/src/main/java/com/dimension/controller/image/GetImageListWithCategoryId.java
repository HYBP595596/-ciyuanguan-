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

@WebServlet(name = "GetImageListWithCategoryId", value = "/GetImageListWithCategoryId")
public class GetImageListWithCategoryId extends BaseImageServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer categoryId = Integer.parseInt(req.getParameter("categoryId"));
        Integer pageNum = PageUtil.getPageNum(req);
        Integer pageSize = PageUtil.getPageSize(req);

        // 1. 查列表
        List<ImageTagTZP> imageTagList = imageService.getImageListWithCategoryId(categoryId, pageNum, pageSize);

        // 2. 查总数（👈 新增）
        long total = imageService.getCategoryImageTotal(categoryId);

        // 3. 组装返回（👈 加上了 total）
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("imageTagList", imageTagList)
                .put("total", total)
                .build());
    }
}