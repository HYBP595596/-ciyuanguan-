package com.dimension.controller.image;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@WebServlet(name = "DownloadImage", value = "/DownloadImage")
public class DownloadImage extends BaseImageServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 直接传入图片url即可
        String url = req.getParameter("url");

        String endpoint = "";
        String accessKeyId = "";
        String accessKeySecret = "vrWVNd6WsTkm84dFBSK5pLlzRj3wyl";
        String bucketName = "dimension-pojo";

        String fileName = url.replace("https://" + bucketName + "." + endpoint + "/", "");
        System.out.println(fileName);

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        InputStream inputStream = ossClient.getObject(bucketName, fileName).getObjectContent();

        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            resp.getOutputStream().write(buffer, 0, length);
        }

        inputStream.close();
        resp.getOutputStream().flush();
    }
}
