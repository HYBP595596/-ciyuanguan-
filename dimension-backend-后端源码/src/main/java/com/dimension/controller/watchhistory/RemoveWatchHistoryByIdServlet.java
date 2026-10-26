package com.dimension.controller.watchhistory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "RemoveWatchHistoryById", value = "/RemoveWatchHistoryById")
public class RemoveWatchHistoryByIdServlet extends BaseWatchHistoryServlet{

    //10.3 /RemoveWatchHistoryById => 根据id删除用户浏览记录

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取数据参数
        Long id = Long.parseLong(req.getParameter("id"));


        //根据id删除收藏记录
        watchHistoryService.removeWatchHistoryById(id);
    }
}
