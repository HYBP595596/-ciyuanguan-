package com.dimension.controller.bookmark;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;
import com.dimension.vo.ImageTagBookmark;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GetUserImageBookmarkList",value = "/GetUserImageBookmarkList")
public class GetUserImageBookmarkList extends BaseImageBookmarkServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = Integer.parseInt(req.getParameter("userId"));
        Integer pageNum = PageUtil.getPageNum(req);
        Integer pageSize = PageUtil.getPageSize(req);

        List<ImageTagBookmark> ImageTagBookmarkList = imageBookmarkService.getUserImageBookmarkList(userId,pageNum,pageSize);
        Long total = imageBookmarkService.getCounttotal(userId);

        Map<Object, Object> result = MapUtil.builder()
                .put("imageTagBookmarkList", ImageTagBookmarkList)
                .put("total",total).build();

        JsonResultOutputUtil.outputJsonResult(resp, result);
    }
}
