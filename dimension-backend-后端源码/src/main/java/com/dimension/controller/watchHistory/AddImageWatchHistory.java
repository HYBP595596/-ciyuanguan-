package com.dimension.controller.watchHistory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(name = "AddImageWatchHistory",value = "/AddImageWatchHistory")
public class AddImageWatchHistory extends BaseWatchHistoryServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long imageid = Long.parseLong(req.getParameter("imageId"));
        Long userid = Long.parseLong(req.getParameter("userId"));

        // 2.建vo，如果有必要的话

        // 3.调用service层获取数据
        watchHistoryService.addImageWatchHistory(imageid, userid);

        // 4.组装数据

        // 5.输出数据

    }
}
