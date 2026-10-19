package com.dimension.controller.watchHistory;

import com.dimension.service.WatchHistoryService;
import com.dimension.service.impl.WatchHistoryServiceImpl;

import javax.servlet.http.HttpServlet;

public class BaseWatchHistoryServlet extends HttpServlet {
    protected WatchHistoryService watchHistoryService = new WatchHistoryServiceImpl();
}
