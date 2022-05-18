package cn.hamster3.service.server.util;

import cn.hamster3.service.common.data.ServicePlayerInfo;
import cn.hamster3.service.server.Bootstrap;
import cn.hamster3.service.server.connection.ServiceCentre;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

public class ServiceUtils {

    public static void initDatabase(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS service_player_info(" +
                "uuid CHAR(36) PRIMARY KEY," +
                "playerName VARCHAR(32)," +
                "bukkitServer VARCHAR(32)," +
                "proxyServer VARCHAR(32)," +
                "online BOOLEAN" +
                ") CHARSET = utf8mb4;");
        statement.close();
        connection.close();
    }

    public static void uploadDataToSQL(DataSource dataSource, File folder) throws SQLException {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("REPLACE INTO service_player_info VALUES(?, ?, ?, ?, ?);");
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                InputStreamReader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8);
                JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
                reader.close();
                ServicePlayerInfo playerInfo = new ServicePlayerInfo(object);
                playerInfo.setOnline(false);
                statement.setString(1, playerInfo.getUuid().toString());
                statement.setString(2, playerInfo.getPlayerName());
                statement.setString(3, playerInfo.getBukkitServer());
                statement.setString(4, playerInfo.getProxyServer());
                statement.setBoolean(5, playerInfo.isOnline());
                statement.addBatch();
            } catch (Exception e) {
                Bootstrap.LOGGER.error("加载存档文件 " + file.getName() + " 时遇到了一个异常: ", e);
            }
            if (i % 100 == 0) {
                statement.executeBatch();
                statement.clearBatch();
                Bootstrap.LOGGER.info("已提交SQL语句。");
            }
            Bootstrap.LOGGER.info("已完成：{}/{}。", i + 1, files.length);
        }

        if (files.length % 100 != 0) {
            statement.executeBatch();
        }
        statement.close();
        connection.close();
    }

    public static void loadPlayerData(ServiceCentre centre) throws SQLException {
        Bootstrap.LOGGER.info("正在加载玩家存档...");
        Connection connection = centre.getDatasource().getConnection();
        Statement statement = connection.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM service_player_info;");
        while (set.next()) {
            ServicePlayerInfo info = new ServicePlayerInfo(
                    UUID.fromString(set.getString("uuid")),
                    set.getString("playerName"),
                    set.getString("bukkitServer"),
                    set.getString("proxyServer"),
                    set.getBoolean("online")
            );
            centre.updatePlayerInfo(info);
        }
        set.close();
        statement.close();
        connection.close();
        Bootstrap.LOGGER.info("玩家存档加载完成.");
    }

    public static void savePlayerData(ServiceCentre centre, ServicePlayerInfo playerInfo) throws SQLException {
        Connection connection = centre.getDatasource().getConnection();
        PreparedStatement statement = connection.prepareStatement("REPLACE INTO service_player_info VALUES(?, ?, ?, ?, ?);");
        statement.setString(1, playerInfo.getUuid().toString());
        statement.setString(2, playerInfo.getPlayerName());
        statement.setString(3, playerInfo.getBukkitServer());
        statement.setString(4, playerInfo.getProxyServer());
        statement.setBoolean(5, playerInfo.isOnline());
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    public static void saveAllPlayerData(ServiceCentre centre) throws SQLException {
        Connection connection = centre.getDatasource().getConnection();
        PreparedStatement statement = connection.prepareStatement("REPLACE INTO service_player_info VALUES(?, ?, ?, ?, ?);");
        ArrayList<ServicePlayerInfo> list = new ArrayList<>(centre.getPlayerInfo().values());
        for (int i = 0; i < list.size(); i++) {
            ServicePlayerInfo playerInfo = list.get(i);
            statement.setString(1, playerInfo.getUuid().toString());
            statement.setString(2, playerInfo.getPlayerName());
            statement.setString(3, playerInfo.getBukkitServer());
            statement.setString(4, playerInfo.getProxyServer());
            statement.setBoolean(5, playerInfo.isOnline());
            statement.addBatch();
            if (i % 100 == 0) {
                statement.executeBatch();
                statement.clearBatch();
                Bootstrap.LOGGER.info("已提交SQL语句。");
            }
        }
        if (list.size() % 100 != 0) {
            statement.executeBatch();
        }
        statement.close();
        connection.close();
    }

    @SuppressWarnings("ConstantConditions")
    public static File saveDefaultFile(String name) {
        File file = new File(name);
        if (file.exists()) {
            return file;
        }
        try {
            Files.copy(Bootstrap.class.getResourceAsStream("/" + name), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            Bootstrap.LOGGER.error("在保存默认配置文件 {} 时遇到了一个错误: {}", name, e);
        }
        return file;
    }
}
