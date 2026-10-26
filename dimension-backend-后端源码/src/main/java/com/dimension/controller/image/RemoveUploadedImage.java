package com.dimension.controller.image;

import cn.hutool.core.map.MapUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * 删除「发布页移除的那张图」对应的服务器文件。
 * 入参 url 形如：
 *   /dimension_dev_01_war_exploded/ShowUploadImage?folder=%E6%8F%92%E7%94%BB&fileName=xxx.jpg
 * 只允许删除上传根目录（次元馆图片）内的文件，并做路径穿越防护。
 */
@WebServlet(name = "RemoveUploadedImage", value = "/RemoveUploadedImage")
public class RemoveUploadedImage extends HttpServlet {

    private static final String ROOT_PATH = "D:\\PG\\JAVA\\次元馆\\-ciyuanguan-\\次元馆图片\\";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getParameter("url");
        String folder = extractParam(url, "folder");
        String fileName = extractParam(url, "fileName");

        boolean deleted = false;
        String reason = null;

        if (fileName == null || fileName.trim().isEmpty()) {
            reason = "缺少文件名";
        }
        if (reason == null && folder == null) {
            folder = "";
        }

        if (reason == null) {
            // 与 ShowUploadImage 一致的基础清洗，后面再用规范路径做严格校验
            folder = folder.replace("..", "").replace("/", "").replace("\\", "").trim();
            fileName = fileName.replace("..", "").replace("/", "").replace("\\", "").trim();

            if (fileName.isEmpty()) {
                reason = "非法文件名";
            } else {
                File root = new File(ROOT_PATH);
                File target = new File(root, folder + File.separator + fileName);
                String rootCanonical = root.getCanonicalPath();
                String prefix = rootCanonical.endsWith(File.separator)
                        ? rootCanonical : rootCanonical + File.separator;
                String targetCanonical = target.getCanonicalPath();

                if (!targetCanonical.startsWith(prefix)) {
                    reason = "非法路径";
                } else if (!target.exists()) {
                    reason = "文件不存在";
                } else if (!target.isFile()) {
                    reason = "不是文件";
                } else {
                    deleted = target.delete();
                    if (!deleted) {
                        reason = "删除失败";
                    }
                }
            }
        }

        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("flag", deleted ? 0 : 1)
                .put("deleted", deleted)
                .put("message", deleted ? "已删除" : (reason == null ? "未删除" : reason))
                .build());
    }

    /** 从 URL 的 query 里取参数（兼容相对地址与带域名地址） */
    private String extractParam(String url, String name) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        int q = url.indexOf('?');
        String query = (q >= 0) ? url.substring(q + 1) : url;
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            if (!name.equals(pair.substring(0, eq))) {
                continue;
            }
            String raw = pair.substring(eq + 1);
            try {
                return URLDecoder.decode(raw, "UTF-8");
            } catch (Exception e) {
                return raw;
            }
        }
        return null;
    }
}
