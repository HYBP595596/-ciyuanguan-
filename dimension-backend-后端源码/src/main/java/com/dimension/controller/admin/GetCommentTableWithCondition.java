package com.dimension.controller.admin;

import com.dimension.controller.comment.BaseCommentServlet;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;
import com.dimension.vo.CommentUserImage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GetCommentTableWithCondition",value = "/GetCommentTableWithCondition")
public class GetCommentTableWithCondition extends BaseCommentServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String nickname = req.getParameter("nickname");
        Integer pageNum = PageUtil.getPageNum(req);
        Integer pageSize = PageUtil.getPageSize(req);

        List<CommentUserImage> commentUserImageList = commentService.getCommentTableWithCondition(nickname,pageNum,pageSize);
        Long total = commentService.gettotal(nickname);

        Map<Object, Object> result = MapUtil.builder()
                .put("commentUserImageList", commentUserImageList)
                .put("total", total).build();

        JsonResultOutputUtil.outputJsonResult(resp, result);
    }
}
