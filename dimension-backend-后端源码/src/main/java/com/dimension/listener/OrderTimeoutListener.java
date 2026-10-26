package com.dimension.listener;

import com.dimension.service.impl.CommissionServiceImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 待支付订单的超时关闭任务。
 *
 * 为什么需要它：用户下单后关掉页面不来付款，订单会一直挂在「待支付」，
 * 支付单也一直可被支付，既占着方案档期又容易产生"超时后付款成功"的对账问题。
 *
 * 两道保险：
 *   1. 这个定时任务每 60 秒扫一次（兜底）
 *   2. 业务侧惰性检查（下单支付、查我的订单时也会顺带关一次），不依赖任务是否准时
 */
public class OrderTimeoutListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(OrderTimeoutListener.class.getName());

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "commission-order-timeout");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                int closed = new CommissionServiceImpl().closeExpiredOrders();
                if (closed > 0) {
                    LOG.info("超时关闭待支付订单 " + closed + " 笔");
                }
            } catch (Throwable e) {
                // 定时任务绝不能把异常抛出去，否则后续调度会被取消
                LOG.log(Level.WARNING, "超时关单任务失败: " + e.getMessage(), e);
            }
        }, 60, 60, TimeUnit.SECONDS);

        LOG.info("订单超时关闭任务已启动（每 60 秒扫描一次）");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
