package com.dimension.controller.image;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 交稿：整包上传（zip/rar/7z/pdf/txt 或单张图片），而不是逐张发图。
 * 返回一个可访问地址，写进订单的 delivery_preview / delivery_final。
 */
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024L * 1024 * 300)
@WebServlet(name = "UploadDeliveryFile", value = "/UploadDeliveryFile")
public class UploadDeliveryFile extends HttpServlet {

    private static final String ROOT_PATH = "D:\\PG\\JAVA\\次元馆\\-ciyuanguan-\\次元馆图片\\";
    private static final String SUB_FOLDER = "交付";
    private static final long MAX_BYTES = 200L * 1024 * 1024; // 200MB
    private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList(
            "zip", "rar", "7z", "tar", "gz", "tgz", "pdf", "txt",
            "jpg", "jpeg", "png", "webp", "gif"
    ));

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Part filePart = req.getPart("file");
            if (filePart == null) {
                out(resp, 1, "请选择要交付的文件", null, null, 0);
                return;
            }
            String original = filePart.getSubmittedFileName();
            if (original == null || original.trim().isEmpty()) {
                out(resp, 1, "文件名无效", null, null, 0);
                return;
            }
            original = new File(original).getName(); // 去掉客户端路径
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot > 0) {
                ext = original.substring(dot + 1).toLowerCase();
            }
            if (!ALLOWED.contains(ext)) {
                out(resp, 1, "不支持的文件类型：" + (ext.isEmpty() ? "无扩展名" : ext)
                        + "（支持 zip/rar/7z/tar/gz/pdf/txt 及常见图片）", null, null, 0);
                return;
            }
            if (filePart.getSize() > MAX_BYTES) {
                out(resp, 1, "交付文件不能超过 200MB", null, null, 0);
                return;
            }

            File dir = new File(ROOT_PATH + SUB_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String newName = IdUtil.simpleUUID() + "-" + original;
            File target = new File(dir, newName);
            try (InputStream in = filePart.getInputStream()) {
                Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            String url = "/dimension_dev_01_war_exploded/ShowUploadImage?folder="
                    + URLEncoder.encode(SUB_FOLDER, "UTF-8")
                    + "&fileName=" + URLEncoder.encode(newName, "UTF-8");
            out(resp, 0, "上传成功", url, original, filePart.getSize());
        } catch (Exception e) {
            out(resp, 1, "上传失败：" + e.getMessage(), null, null, 0);
        }
    }

    private void out(HttpServletResponse resp, int flag, String message,
                     String url, String fileName, long size) throws IOException {
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("flag", flag)
                .put("message", message)
                .put("url", url)
                .put("fileName", fileName)
                .put("size", size)
                .build());
    }
}
