package com.dimension.controller.comment;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;
import com.dimension.vo.CommentUser;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name="GetImageCommentList" ,value ="/GetImageCommentList")
public class GetImageCommentList extends BaseCommentServlet{

    @Override
    protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
            Long imageId=Long.valueOf(req.getParameter("imageId"));
            Integer pageSize= PageUtil.getPageSize(req);
            Integer pageNum= PageUtil.getPageNum(req);
        List<CommentUser> commentUserList=commentService.getCommentUser(imageId,pageNum,pageSize);
        long total = commentService.getCommentListTotal(imageId);

        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder().put("commentUserList",commentUserList)
                .put("total", total).build());
    }
}
