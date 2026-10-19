package com.dimension.utils;

import cn.hutool.json.JSONUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonResultOutputUtil {
    public static void outputJsonResult(HttpServletResponse res, Object o) throws IOException {
        res.getWriter().write(JSONUtil.toJsonStr(o));
    }
}
