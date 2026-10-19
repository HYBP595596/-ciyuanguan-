package com.dimension.controller.image;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.dimension.utils.JsonResultOutputUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 50)
@WebServlet(name = "UploadImageOrUserAvatar", value = "/UploadImageOrUserAvatar")
public class UploadImageOrUserAvatar extends BaseImageServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String endpoint = "oss-cn-beijing.aliyuncs.com";
        String accessKeyId = "";
        String accessKeySecret = "";
        String bucketName = "dimension-pojo";

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        Part filePart = req.getPart("file");
        String fileName = filePart.getSubmittedFileName();
        InputStream inputStream = filePart.getInputStream();
        String fileDirectory = "次元馆/" + DateUtil.format(new Date(), "yyyy-MM/dd/");
        String newFileName = fileDirectory + IdUtil.simpleUUID() + "-" + fileName;

        ossClient.putObject(bucketName, newFileName, inputStream);
        ossClient.shutdown();

        String url = "https://" + bucketName + "." + endpoint + "/" + newFileName;
        JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                .put("url", url).build());
    }
}
