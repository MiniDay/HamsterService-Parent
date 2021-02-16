package cn.hamster3.service.common.entity;

/**
 * 消息发送者类型
 */
@SuppressWarnings("unused")
public enum ServiceSenderType {
    /**
     * Bukkit服务器
     */
    BUKKIT,
    /**
     * BungeeCord 等代理服务器
     */
    PROXY,
    /**
     * 消息中心服务器（名称为：ServiceCentre）
     */
    SERVICE_CENTRE
}
