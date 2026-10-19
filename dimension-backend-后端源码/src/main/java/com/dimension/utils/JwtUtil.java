package com.dimension.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.dimension.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {

//    public static final long EXPIRE = 1000 * 60 * 30L; // 半小时
    public static final long EXPIRE = 1000 * 60 * 60 * 24 * 30L; // 30天 - 要加个L啊，不然先计算int然后再转的，会超出范围
    public static final String SECRET = "ukc8BDbRigUDaY6pZFfWus2jZWLPHO";

    public static String createToken(Object o) {
        String JwtToken = Jwts.builder()
                //JWT头信息
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS2256")
                .setSubject("lx-user")

                // 主体
                .claim("o", JSONUtil.toJsonStr(o))

                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))

                // 签名
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
        return JwtToken;
    }

    public static boolean checkToken(String jwtToken){
        if (StrUtil.isEmpty(jwtToken)){
            return false;
        }
        try{
            //验证token
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(jwtToken);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String getObject(String token){
        try {
            if (StrUtil.isEmpty(token)){
                return null;
            }
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
            Claims body = claimsJws.getBody();

            // 获取o
            String o = (String) body.get("o");
            return o;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isNull(String token) {
        String object = getObject(token);
        if (StrUtil.isEmpty(object)) {
            return true;
        }
        return false;
    }
}
