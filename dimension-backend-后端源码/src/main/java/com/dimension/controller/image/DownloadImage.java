package com.dimension.controller.image;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 作品下载：支持两种地址
 *   1. 本地上传的文件：/dimension_dev_01_war_exploded/ShowUploadImage?folder=插画&fileName=xxx.png
 *   2. 外链（OSS 等）：https://...
 * 一律以附件形式返回，并尽量还原原始文件名。
 */
@WebServlet(name = "DownloadImage", value = "/DownloadImage")
public class DownloadImage extends HttpServlet {

    private static final String ROOT_PATH = "D:\\PG\\JAVA\\次元馆\\-ciyuanguan-\\次元馆图片\\";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getParameter("url");
        if (url == null || url.trim().isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write("缺少 url 参数");
            return;
        }
        String target = url.trim();
        try {
            if (target.startsWith("http://") || target.startsWith("https://")) {
                downloadRemote(target, resp);
            } else {
                downloadLocal(target, resp);
            }
        } catch (Exception e) {
            if (!resp.isCommitted()) {
                resp.setStatus(500);
                resp.getWriter().write("下载失败：" + e.getMessage());
            }
        }
    }

    // ================= 本地文件 =================
    private void downloadLocal(String target, HttpServletResponse resp) throws IOException {
        String folder = extractParam(target, "folder");
        String fileName = extractParam(target, "fileName");
        if (fileName == null || fileName.trim().isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write("无法解析文件地址");
            return;
        }
        folder = folder == null ? "" : folder.replace("..", "").replace("/", "").replace("\\", "").trim();
        fileName = fileName.replace("..", "").replace("/", "").replace("\\", "").trim();

        File root = new File(ROOT_PATH);
        File file = new File(root, folder + File.separator + fileName);
        String rootCanonical = root.getCanonicalPath();
        String prefix = rootCanonical.endsWith(File.separator) ? rootCanonical : rootCanonical + File.separator;
        if (!file.getCanonicalPath().startsWith(prefix) || !file.isFile()) {
            resp.setStatus(404);
            resp.getWriter().write("文件不存在");
            return;
        }

        String displayName = stripUuid(file.getName());
        writeHeader(resp, displayName, file.length());
        try (InputStream in = new FileInputStream(file); OutputStream out = resp.getOutputStream()) {
            copy(in, out);
        }
    }

    // ================= 外链（OSS） =================
    private void downloadRemote(String target, HttpServletResponse resp) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(target).openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(20000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 DimensionDownloader");
        conn.setInstanceFollowRedirects(true);
        int code = conn.getResponseCode();
        if (code >= 400) {
            resp.setStatus(502);
            resp.getWriter().write("远端资源不可用（HTTP " + code + "）");
            conn.disconnect();
            return;
        }

        String path = conn.getURL().getPath();
        String remoteName = path.substring(path.lastIndexOf('/') + 1);
        if (remoteName.isEmpty()) {
            remoteName = "download";
        }
        long length = conn.getContentLengthLong();

        writeHeader(resp, stripUuid(remoteName), length);
        try (InputStream in = conn.getInputStream(); OutputStream out = resp.getOutputStream()) {
            copy(in, out);
        } finally {
            conn.disconnect();
        }
    }

    // ================= 公共 =================
    private void writeHeader(HttpServletResponse resp, String displayName, long length) {
        String name = (displayName == null || displayName.isEmpty()) ? "download" : displayName;
        String encoded;
        try {
            encoded = URLEncoder.encode(name, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            encoded = "download";
        }
        resp.setContentType(contentTypeOf(name));
        if (length > 0) {
            resp.setContentLengthLong(length);
        }
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"download\"; filename*=UTF-8''" + encoded);
    }

    private String contentTypeOf(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".txt")) return "text/plain; charset=UTF-8";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }

    /** 去掉上传时加在前面的 32 位 uuid 前缀，还原成用户原本的文件名 */
    private String stripUuid(String name) {
        if (name != null && name.length() > 33 && name.charAt(32) == '-') {
            String head = name.substring(0, 32);
            if (head.matches("[0-9a-fA-F]{32}")) {
                return name.substring(33);
            }
        }
        return name;
    }

    private String extractParam(String url, String name) {
        if (url == null) return null;
        int q = url.indexOf('?');
        String query = q >= 0 ? url.substring(q + 1) : url;
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq <= 0) continue;
            if (!name.equals(pair.substring(0, eq))) continue;
            try {
                return URLDecoder.decode(pair.substring(eq + 1), "UTF-8");
            } catch (Exception e) {
                return pair.substring(eq + 1);
            }
        }
        return null;
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        out.flush();
    }
}
