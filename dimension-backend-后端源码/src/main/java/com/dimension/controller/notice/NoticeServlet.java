package com.dimension.controller.notice;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.Notice;
import com.dimension.mapper.NoticeMapper;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;
import com.dimension.utils.SessionUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 全站公告 /Notice?action=xxx
 *   list    分页列表（前台/后台通用）
 *   latest  最新几条（侧栏/首页用）
 *   add     发布（管理员）
 *   delete  删除（管理员）
 */
@WebServlet(name = "Notice", value = "/Notice")
public class NoticeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        if (action == null) action = "";
        try {
            NoticeMapper mapper = SessionUtil.getMapper(NoticeMapper.class);
            switch (action) {
                case "list": {
                    Integer pageNum = PageUtil.getPageNum(req);
                    Integer pageSize = PageUtil.getPageSize(req);
                    List<Notice> list = mapper.getNoticeList(pageNum, pageSize);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("noticeList", list)
                            .put("total", mapper.getNoticeTotal())
                            .build());
                    return;
                }
                case "latest": {
                    String l = req.getParameter("limit");
                    int limit = (l == null || l.trim().isEmpty()) ? 3 : Integer.parseInt(l);
                    if (limit <= 0 || limit > 50) limit = 3;
                    List<Notice> list = mapper.getLatestNotices(limit);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("noticeList", list).build());
                    return;
                }
                case "add": {
                    Long adminId = Long.valueOf(req.getParameter("adminId"));
                    String title = req.getParameter("title");
                    String content = req.getParameter("content");
                    if (title == null || title.trim().isEmpty()) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("message", "请填写公告标题").build());
                        return;
                    }
                    if (content == null || content.trim().isEmpty()) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("message", "请填写公告内容").build());
                        return;
                    }
                    Notice notice = new Notice();
                    notice.setAdminId(adminId);
                    notice.setTitle(title.trim());
                    notice.setContent(content.trim());
                    mapper.insertNotice(notice);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0).put("id", notice.getId()).put("message", "已发布").build());
                    return;
                }
                case "delete": {
                    Long id = Long.valueOf(req.getParameter("id"));
                    int rows = mapper.deleteNotice(id);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", rows > 0 ? 0 : 1).build());
                    return;
                }
                default:
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 1).put("message", "未知操作: " + action).build());
            }
        } catch (Exception e) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1).put("message", "参数错误: " + e.getMessage()).build());
        }
    }
}
