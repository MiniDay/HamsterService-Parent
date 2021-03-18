package cn.hamster3.service.bukkit.api;

import cn.hamster3.service.bukkit.handler.ServiceConnection;
import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceSenderInfo;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

/**
 * 服务信息API
 * <p>
 * 通过这个类的静态方法可以快速查询其他服务器的状态
 * <p>
 * 也可以获取已连接至服务器的全部玩家信息（即使玩家在其他服务器）
 */
@SuppressWarnings("unused")
public class ServiceInfoAPI {
    private static final HashSet<ServicePlayerInfo> playerInfo = new HashSet<>();
    private static final HashSet<ServiceSenderInfo> senderInfo = new HashSet<>();
    private static ServiceConnection connection;

    /**
     * 这个类不应该由你实例化
     * <p>
     * 无论什么时候都不要调用这个类的构造方法
     *
     * @param connection 连接对象
     */
    public ServiceInfoAPI(ServiceConnection connection) {
        if (ServiceInfoAPI.connection != null) {
            throw new IllegalStateException("不允许重复初始化 ServiceMessageAPI !");
        }
        ServiceInfoAPI.connection = connection;

    }

    /**
     * 获取玩家信息，当玩家不在线时返回 null
     *
     * @param uuid 玩家的UUID
     * @return 玩家信息
     */
    public static ServicePlayerInfo getPlayerInfo(@NotNull UUID uuid) {
        synchronized (playerInfo) {
            for (ServicePlayerInfo info : playerInfo) {
                if (info.getUuid().equals(uuid)) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * 获取玩家信息，当玩家不在线时返回 null
     *
     * @param playerName 玩家ID
     * @return 玩家信息
     */
    public static ServicePlayerInfo getPlayerInfo(@NotNull String playerName) {
        synchronized (playerInfo) {
            for (ServicePlayerInfo info : playerInfo) {
                if (info.getPlayerName().equalsIgnoreCase(playerName)) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * 获取全部在线玩家的信息
     *
     * @return 玩家们的信息
     */
    public static HashSet<ServicePlayerInfo> getOnlinePlayers() {
        HashSet<ServicePlayerInfo> set = new HashSet<>();
        synchronized (playerInfo) {
            for (ServicePlayerInfo info : playerInfo) {
                if (info.isOnline()) {
                    set.add(info);
                }
            }
        }
        return set;
    }

    /**
     * 获取全部在线玩家的信息
     *
     * @return 玩家们的信息
     */
    public static HashSet<ServicePlayerInfo> getAllPlayerInfo() {
        synchronized (playerInfo) {
            return new HashSet<>(playerInfo);
        }
    }


    /**
     * 获取服务端信息
     *
     * @param senderName 服务端id
     * @return 服务端信息
     */
    public static ServiceSenderInfo getSenderInfo(String senderName) {
        synchronized (senderInfo) {
            for (ServiceSenderInfo info : senderInfo) {
                if (info.getName().equalsIgnoreCase(senderName)) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * 获取所有连接至服务中心的信息
     *
     * @return 服务器信息
     */
    public static HashSet<ServiceSenderInfo> getAllSenderInfo() {
        synchronized (senderInfo) {
            return new HashSet<>(senderInfo);
        }
    }

    /**
     * 获取当前服务器的名称
     *
     * @return 服务器id
     */
    public static String getLocalServerName() {
        return connection.getInfo().getName();
    }

    /**
     * 获取当前服务器的别名
     *
     * @return 服务器别名
     */
    public static String getLocalServerNickName() {
        return connection.getInfo().getNickName();
    }

    /**
     * 获取当前服务器的SenderInfo对象
     *
     * @return 当前服务器的发送者信息
     */
    public static ServiceSenderInfo getLocalSenderInfo() {
        return connection.getInfo();
    }

    /**
     * （当玩家进入子服时）设置一条玩家信息
     *
     * @param playerInfo 玩家信息
     */
    public void loadPlayerInfo(ServicePlayerInfo playerInfo) {
        synchronized (ServiceInfoAPI.playerInfo) {
            ServiceInfoAPI.playerInfo.remove(playerInfo);
            ServiceInfoAPI.playerInfo.add(playerInfo);
        }
    }

    /**
     * 加载一条服务器信息
     *
     * @param senderInfo 服务器信息
     */
    public void loadServerInfo(ServiceSenderInfo senderInfo) {
        synchronized (ServiceInfoAPI.senderInfo) {
            ServiceInfoAPI.senderInfo.remove(senderInfo);
            ServiceInfoAPI.senderInfo.add(senderInfo);
        }
    }

    /**
     * 删除一条服务器信息
     *
     * @param name 服务器名称
     */
    public void removeSenderInfo(String name) {
        ServiceSenderInfo info = getSenderInfo(name);
        synchronized (ServiceInfoAPI.senderInfo) {
            senderInfo.remove(info);
        }
    }

    /**
     * 重设所有玩家信息
     *
     * @param playerInfo 玩家们的信息
     */
    public void resetAllPlayerInfo(HashSet<ServicePlayerInfo> playerInfo) {
        synchronized (ServiceInfoAPI.playerInfo) {
            ServiceInfoAPI.playerInfo.clear();
            ServiceInfoAPI.playerInfo.addAll(playerInfo);
        }
    }

    /**
     * 重设所有服务器信息
     *
     * @param senderInfo 所有服务器的信息
     */
    public void resetAllServerInfo(HashSet<ServiceSenderInfo> senderInfo) {
        synchronized (ServiceInfoAPI.senderInfo) {
            ServiceInfoAPI.senderInfo.clear();
            ServiceInfoAPI.senderInfo.addAll(senderInfo);
        }
    }
}
