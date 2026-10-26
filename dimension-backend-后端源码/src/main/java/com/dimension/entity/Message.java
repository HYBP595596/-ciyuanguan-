package com.dimension.entity;

import java.util.Date;

/**
 * 私信：对应 dimension_message
 */
public class Message {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer isRead;   // 0未读 1已读
    private Date createTime;

    // 非表字段：会话列表用
    private Long peerId;
    private String peerNickname;
    private String peerAvatar;
    private Integer unreadCount;

    public Long getPeerId() { return peerId; }
    public void setPeerId(Long peerId) { this.peerId = peerId; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getPeerNickname() { return peerNickname; }
    public void setPeerNickname(String peerNickname) { this.peerNickname = peerNickname; }
    public String getPeerAvatar() { return peerAvatar; }
    public void setPeerAvatar(String peerAvatar) { this.peerAvatar = peerAvatar; }
    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }
}
