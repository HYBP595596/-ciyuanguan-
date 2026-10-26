package com.dimension.controller.imageCategory;

import cn.hutool.core.map.MapUtil;
import com.dimension.controller.image.BaseImageServlet;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.PageUtil;
import com.dimension.vo.ImageTagTZP;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

/**
 * 后台「分类管理」：管理作品所属的类型（插画/漫画/小说）
 *   /CategoryAdmin?action=images&categoryId=1&currentPage=1&pageSize=10
 *   /CategoryAdmin?action=move&imageIds=1,2,3&categoryId=2
 */
@WebServlet(name = "CategoryAdmin", value = "/CategoryAdmin")
public class CategoryAdminServlet extends BaseImageServlet {

    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest req,
                         javax.servlet.http.HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(javax.servlet.http.HttpServletRequest req,
                          javax.servlet.http.HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    private void handle(javax.servlet.http.HttpServletRequest req,
                        javax.servlet.http.HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        if (action == null) action = "";
        try {
            switch (action) {
                case "images": {
                    Integer categoryId = Integer.valueOf(req.getParameter("categoryId"));
                    Integer pageNum = PageUtil.getPageNum(req);
                    Integer pageSize = PageUtil.getPageSize(req);
                    List<ImageTagTZP> list = imageService.getAdminImagesByCategory(categoryId, pageNum, pageSize);
                    long total = imageService.getAdminCategoryImageTotal(categoryId);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("imageTagList", list)
                            .put("total", total)
                            .build());
                    return;
                }
                case "move": {
                    String ids = req.getParameter("imageIds");
                    Integer categoryId = Integer.valueOf(req.getParameter("categoryId"));
                    if (ids == null || ids.trim().isEmpty()) {
                        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                                .put("flag", 1).put("message", "请选择要移动的作品").build());
                        return;
                    }
                    int moved = 0;
                    for (String s : ids.split(",")) {
                        String t = s.trim();
                        if (t.isEmpty()) continue;
                        if (imageService.moveImageCategory(Long.valueOf(t), categoryId)) {
                            moved++;
                        }
                    }
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 0)
                            .put("moved", moved)
                            .put("message", "已移动 " + moved + " 个作品")
                            .build());
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
