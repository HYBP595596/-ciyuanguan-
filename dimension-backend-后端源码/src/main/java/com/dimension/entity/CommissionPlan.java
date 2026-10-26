package com.dimension.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 画师的约稿方案（档期）：对应 dimension_commission_plan
 */
public class CommissionPlan {
    private Long id;
    private Long artistId;
    private String title;          // 方案标题，如「Q版头像」
    private BigDecimal price;      // 价格
    private Integer deliveryDays;  // 交稿周期(天)
    private String coverUrl;       // 方案示例图
    private Integer status;        // 0待审核 1上架中 2下架
    private Integer contentType;   // 约稿类型: 1插画 2漫画 3小说
    private Date createTime;

    // 非表字段：查询时带出画师信息，方便前端展示
    private String artistNickname;
    private String artistAvatar;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getDeliveryDays() { return deliveryDays; }
    public void setDeliveryDays(Integer deliveryDays) { this.deliveryDays = deliveryDays; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getContentType() { return contentType; }
    public void setContentType(Integer contentType) { this.contentType = contentType; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getArtistNickname() { return artistNickname; }
    public void setArtistNickname(String artistNickname) { this.artistNickname = artistNickname; }
    public String getArtistAvatar() { return artistAvatar; }
    public void setArtistAvatar(String artistAvatar) { this.artistAvatar = artistAvatar; }
}
