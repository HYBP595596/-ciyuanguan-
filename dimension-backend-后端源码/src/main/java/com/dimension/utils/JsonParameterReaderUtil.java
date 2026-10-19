package com.dimension.utils;

import cn.hutool.json.JSONUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class JsonParameterReaderUtil {
    public static Map readJson(HttpServletRequest req) throws IOException {
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        BufferedReader reader = req.getReader();
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        String json = jsonBuilder.toString();
        return JSONUtil.toBean(json, Map.class);
    }
}
