package com.dimension.controller.image;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;
import com.dimension.vo.ImageTagTZP;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetPendingImage", value = "/GetPendingImage")
public class GetPendingImageServlet extends BaseImageServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer pageNum = PageUtil.getPageNum(req);
        Integer pageSize = PageUtil.getPageSize(req);

        // 审核页需要看到标签，所以返回「图片 + 标签」的结构，而不是裸图片
        List<ImageTagTZP> list = imageService.getPendingImageTagList(pageNum, pageSize);
        long total = imageService.getPendingTotal();

        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("imageList", list)
                .put("total", total)
                .build());
    }
}
