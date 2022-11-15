package cn.hamster3.service.bukkit.api;

import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.common.entity.ServiceSenderInfo;
import cn.hamster3.service.common.entity.ServiceSenderType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
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
    public static final ServiceSenderInfo localInfo = new ServiceSenderInfo(ServiceSenderType.BUKKIT, "local", "服务器");

    /**
     * 这个类不应该由你实例化
     * <p>
     * 无论什么时候都不要调用这个类的构造方法
     */
    public ServiceInfoAPI() {
    }

    /**
     * 获取玩家信息，当玩家不在线时返回 null
     *
     * @param uuid 玩家的UUID
     * @return 玩家信息
     */
    @SuppressWarnings("ConstantConditions")
    public static ServicePlayerInfo getPlayerInfo(@NotNull UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player == null) {
            return null;
        }
        return new ServicePlayerInfo(player.getUniqueId(), player.getName(), "local", "", player.isOnline());
    }

    /**
     * 获取玩家信息，当玩家不在线时返回 null
     *
     * @param playerName 玩家ID
     * @return 玩家信息
     */
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    public static ServicePlayerInfo getPlayerInfo(@NotNull String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) {
            return null;
        }
        return new ServicePlayerInfo(player.getUniqueId(), player.getName(), "local", "", player.isOnline());
    }

    /**
     * 获取全部在线玩家的信息
     *
     * @return 玩家们的信息
     */
    public static HashSet<ServicePlayerInfo> getOnlinePlayers() {
        HashSet<ServicePlayerInfo> set = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            set.add(new ServicePlayerInfo(player.getUniqueId(), player.getName(), "local", "", true));
        }
        return set;
    }

    /**
     * 获取全部在线玩家的信息
     *
     * @return 玩家们的信息
     */
    @SuppressWarnings("ConstantConditions")
    public static HashSet<ServicePlayerInfo> getAllPlayerInfo() {
        HashSet<ServicePlayerInfo> set = new HashSet<>();
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            set.add(new ServicePlayerInfo(player.getUniqueId(), player.getName(), "local", "", player.isOnline()));
        }
        return set;
    }

    /**
     * 获取服务端信息
     *
     * @param senderName 服务端id
     * @return 服务端信息
     */
    public static ServiceSenderInfo getSenderInfo(String senderName) {
        if (senderName.equals("local")) {
            return localInfo;
        }
        return null;
    }

    /**
     * 获取所有连接至服务中心的信息
     *
     * @return 服务器信息
     */
    public static HashSet<ServiceSenderInfo> getAllSenderInfo() {
        HashSet<ServiceSenderInfo> set = new HashSet<>();
        set.add(localInfo);
        return set;
    }

    /**
     * 获取当前服务器的名称
     *
     * @return 服务器id
     */
    public static String getLocalServerName() {
        return localInfo.getName();
    }

    /**
     * 获取当前服务器的别名
     *
     * @return 服务器别名
     */
    public static String getLocalServerNickName() {
        return localInfo.getNickName();
    }

    /**
     * 获取当前服务器的SenderInfo对象
     *
     * @return 当前服务器的发送者信息
     */
    public static ServiceSenderInfo getLocalSenderInfo() {
        return localInfo;
    }

    /**
     * （当玩家进入子服时）设置一条玩家信息
     *
     * @param playerInfo 玩家信息
     */
    public void loadPlayerInfo(ServicePlayerInfo playerInfo) {
    }

    /**
     * 加载一条服务器信息
     *
     * @param senderInfo 服务器信息
     */
    public void loadServerInfo(ServiceSenderInfo senderInfo) {
    }

    /**
     * 删除一条服务器信息
     *
     * @param name 服务器名称
     */
    public void removeSenderInfo(String name) {
    }

    /**
     * 重设所有玩家信息
     *
     * @param playerInfo 玩家们的信息
     */
    public void resetAllPlayerInfo(HashSet<ServicePlayerInfo> playerInfo) {
    }

    /**
     * 重设所有服务器信息
     *
     * @param senderInfo 所有服务器的信息
     */
    public void resetAllServerInfo(HashSet<ServiceSenderInfo> senderInfo) {
    }
}
