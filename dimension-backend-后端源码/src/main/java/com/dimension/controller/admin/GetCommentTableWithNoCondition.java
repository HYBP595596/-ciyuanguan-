package com.dimension.controller.admin;

import com.dimension.controller.comment.BaseCommentServlet;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;
import com.dimension.vo.CommentUserImage;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetCommentTableWithNoCondition",value = "/GetCommentTableWithNoCondition")
public class GetCommentTableWithNoCondition extends BaseCommentServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer pageSize= PageUtil.getPageSize(req);
        Integer pageNum=PageUtil.getPageNum(req);
        List<CommentUserImage> commentUserImageList=commentService.getCommentUserImage(pageNum,pageSize);
//     int total=commentUserImageList.size();

        long total = commentService.getTotalWithNoCondition();

//        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder().put("commentUserImageList",commentUserImageList).build());
//        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder().put("total",total).build());

        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("commentUserImageList", commentUserImageList)
                .put("total", total).build());
    }
}
