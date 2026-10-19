package com.dimension.controller.watchHistory;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;
import com.dimension.vo.ImageTagHistoryCategory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GetUserWatchHistory",value = "/GetUserWatchHistory")
public class GetUserWatchHistory extends BaseWatchHistoryServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1.获取数据
        Long userid = Long.parseLong(req.getParameter("userId"));
        Integer pageNum = PageUtil.getPageNum(req);
        Integer pageSize = PageUtil.getPageSize(req);
        // 2.建vo，如果有必要的话
        //ImageTagHistoryCategory用于存放总数据
        //ImageHistoryCategory用于存放除Tag的数据，用于后面组装
        // 3.调用service层获取数据
        List<ImageTagHistoryCategory> imageTagHistoryCategoryList = watchHistoryService.getUserWatchHistory(userid,pageNum,pageSize);

        long total = watchHistoryService.getTotal(userid);

        // 4.组装数据
        Map<Object, Object> result = MapUtil.builder()
                .put("imageTagHistoryCategoryList", imageTagHistoryCategoryList)
                .put("total", total).build();
        // 5.输出数据
        JsonResultOutputUtil.outputJsonResult(resp, result);
    }
}
