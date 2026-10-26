package com.dimension.controller.image;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption; // 👈 新增导入，用于覆盖文件

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 200)
@WebServlet(name = "UploadImageOrUserAvatar", value = "/UploadImageOrUserAvatar")
public class UploadImageOrUserAvatar extends BaseImageServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Part filePart = null;
        try {
            filePart = req.getPart("file");
        } catch (ServletException | IllegalStateException e) {
            // 不是 multipart 请求（或超出 @MultipartConfig 限制）：按业务错误返回，不要抛 500
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1)
                    .put("message", "上传格式不正确，请用表单方式上传文件")
                    .build());
            return;
        }
        if (filePart == null || filePart.getSize() == 0) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1)
                    .put("message", "没有收到文件，请重新选择后上传")
                    .build());
            return;
        }
        String fileName = filePart.getSubmittedFileName();

        // 1. 获取前端传来的类型：avatar / cover / illust / novel
        String type = req.getParameter("type");

        // 小说正文限制 5MB（前端也会拦，这里是服务端兜底）
        long MAX_NOVEL_BYTES = 5L * 1024 * 1024;
        if ("novel".equals(type) && filePart.getSize() > MAX_NOVEL_BYTES) {
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("message", "正文文件不能超过 5MB（约 170 万字）")
                    .build());
            return;
        }

        String subFolder = "";
        if ("avatar".equals(type)) {
            subFolder = "用户头像";
        } else if ("cover".equals(type)) {
            subFolder = "用户背景";
        } else if ("novel".equals(type)) {
            // 小说正文(.txt)单独放一个目录，和插画/漫画分开
            subFolder = "小说";
        } else {
            subFolder = "插画";
        }

        // 👇 新增逻辑：获取 userId，如果是头像或背景，用固定名字覆盖
        String userId = req.getParameter("userId");
        String newFileName;

        if (userId != null && !userId.isEmpty() && ("avatar".equals(type) || "cover".equals(type))) {
            // 取原文件的后缀名（比如 .jpg / .png）
            String ext = "";
            if (fileName != null && fileName.lastIndexOf(".") > 0) {
                ext = fileName.substring(fileName.lastIndexOf("."));
            }
            // 固定名字：例如 1_avatar.jpg
            newFileName = userId + "_" + type + ext;
        } else {
            // 如果是插画或者没有传 userId，保留原来的 UUID 随机命名
            newFileName = IdUtil.simpleUUID() + "-" + fileName;
        }
        // 👆 新增结束

        // 2. 拼接绝对路径
        String rootPath = "D:\\PG\\JAVA\\次元馆\\-ciyuanguan-\\次元馆图片\\";
        String uploadDirPath = rootPath + subFolder;

        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File targetFile = new File(uploadDir, newFileName);
        try (InputStream inputStream = filePart.getInputStream()) {
            // 👇 重要：加上 REPLACE_EXISTING，这样如果有同名文件会直接覆盖，不会报错
            Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // 3. 返回访问地址（文件夹名是中文，需要 URLEncode 编码）
        // 必须原封不动照抄
        String encodedFolder = URLEncoder.encode(subFolder, "UTF-8");
        String url = "/dimension_dev_01_war_exploded/ShowUploadImage?folder=" + encodedFolder + "&fileName=" + newFileName;
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("url", url).build());
    }
}