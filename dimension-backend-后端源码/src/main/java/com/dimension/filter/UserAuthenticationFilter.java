package com.dimension.filter;

import cn.hutool.core.map.MapUtil;
import com.dimension.entity.Admin;
import com.dimension.entity.User;
import com.dimension.utils.AdminAuthUtil;
import com.dimension.utils.JsonResultOutputUtil;
import com.dimension.utils.UserAuthUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 普通用户鉴权过滤器。
 *
 * 策略：只保护"写操作/私有数据"这类必须登录的接口，浏览类接口保持公开。
 * 通过条件（满足其一）：
 *   1. 携带有效的用户 token（Authentication）
 *   2. 携带有效的管理员 token（AdminAuthentication）——后台也会复用部分用户接口（如上传作品）
 *
 * 额外防护：同时带了用户 token 且请求里有 userId/buyerId 参数时，
 * 会校验它必须等于 token 里的用户 id，防止"登录了就能操作别人的数据"。
 */
public class UserAuthenticationFilter implements Filter {

    /** 必须登录（用户或管理员）才能访问 */
    private static final Set<String> PROTECTED = new HashSet<>(Arrays.asList(
            // 账号
            "/ApplyArtist", "/UpdateUserInfo", "/UpdateUserPassword",
            "/UpdateUserAvatar", "/UpdateUserCover", "/GetUserRateCase",
            // 作品
            "/AddImage", "/CommentImage", "/UploadImageOrUserAvatar", "/RemoveUploadedImage",
            // 收藏
            "/AddImageBookmark", "/RemoveImageBookmark", "/GetimageBookmark",
            "/GetUserImageBookmarkList",
            // 浏览历史
            "/AddImageWatchHistory", "/RemoveWatchHistoryById", "/GetUserWatchHistory",
            // 评分
            "/LikeImage", "/DislikeImage", "/CancelRateImage",
            // 关注
            "/FollowUser", "/UnfollowUser",
            // 私信 / 消息中心
            "/Message", "/Inbox",
            // 约稿（create/myOrders/confirm）+ 画师侧 + 支付
            "/Commission", "/Artist", "/Payment",
            // 约稿交付包上传
            "/UploadDeliveryFile"
    ));

    /** Commission 里这些 action 是公开的（约稿广场、方案列表）；订单详情/下单/确认都要登录 */
    private static final Set<String> PUBLIC_ACTIONS = new HashSet<>(Arrays.asList(
            "artists", "artistPlans", "onlinePlans"
    ));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        // 跨域预检不带自定义头，必须放行
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        String path = req.getServletPath();
        boolean needAuth = path != null && PROTECTED.contains(path);

        // 约稿：广场、画师方案、订单详情是公开的
        if (needAuth && "/Commission".equals(path)) {
            String action = req.getParameter("action");
            needAuth = action == null || !PUBLIC_ACTIONS.contains(action);
        }

        if (!needAuth) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        User user = UserAuthUtil.resolve(UserAuthUtil.extractToken(req));
        Admin admin = AdminAuthUtil.resolve(AdminAuthUtil.extractToken(req));

        if (user == null && admin == null) {
            resp.setStatus(401);
            JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                    .put("flag", 1)
                    .put("needLogin", true)
                    .put("message", "请先登录")
                    .build());
            return;
        }

        // 普通用户不能操作他人数据（管理员不受此限制）
        // 约定：请求里只要带了"身份参数"，就必须等于 token 里的用户 id
        if (user != null && admin == null) {
            String[] identityParams = new String[]{"userId", "buyerId", "artistId", "senderId"};
            for (String name : identityParams) {
                String value = req.getParameter(name);
                if (value == null || value.trim().isEmpty()) {
                    continue;
                }
                if (!value.trim().equals(String.valueOf(user.getId()))) {
                    resp.setStatus(403);
                    JsonResultOutputUtil.outputJsonResult(resp, MapUtil.builder()
                            .put("flag", 1)
                            .put("message", "不能操作其他用户的数据")
                            .build());
                    return;
                }
            }
        }

        if (user != null) {
            req.setAttribute("currentUser", user);
        }
        if (admin != null) {
            req.setAttribute("currentAdmin", admin);
        }
        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
