package com.dimension.controller.image;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet(name = "ShowUploadImage", value = "/ShowUploadImage")
public class ShowUploadImage extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 接收前端传来的文件夹名和图片名
        String folder = req.getParameter("folder");
        String fileName = req.getParameter("fileName");

        if (fileName == null || fileName.isEmpty()) return;
        if (folder == null) folder = "";

        // 安全校验：防止别人恶意读取你电脑里的其他文件
        folder = folder.replace("..", "").replace("/", "").replace("\\", "");

        // 拼接你本地物理磁盘的绝对路径
        String rootPath = "D:\\PG\\JAVA\\次元馆\\-ciyuanguan-\\次元馆图片\\";
        File file = new File(rootPath + folder, fileName);

        // 如果文件存在，就读取并传给浏览器
        if (file.exists()) {
            // 按扩展名给内容类型：小说正文 .txt、交付包 .zip 都不能当成图片
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".txt")) {
                resp.setContentType("text/plain; charset=UTF-8");
            } else if (lower.endsWith(".png")) {
                resp.setContentType("image/png");
            } else if (lower.endsWith(".gif")) {
                resp.setContentType("image/gif");
            } else if (lower.endsWith(".webp")) {
                resp.setContentType("image/webp");
            } else if (lower.endsWith(".pdf")) {
                resp.setContentType("application/pdf");
            } else if (lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")
                    || lower.endsWith(".tar") || lower.endsWith(".gz") || lower.endsWith(".tgz")) {
                // 交付压缩包：按下载处理
                resp.setContentType("application/octet-stream");
                resp.setHeader("Content-Disposition",
                        "attachment; filename=\"" + java.net.URLEncoder.encode(fileName, "UTF-8") + "\"");
            } else {
                resp.setContentType("image/jpeg");
            }
            resp.setContentLength((int) file.length());

            try (FileInputStream in = new FileInputStream(file);
                 OutputStream out = resp.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
        } else {
            // 文件不存在就报 404，前端就会显示白板
            resp.setStatus(404);
        }
    }
}