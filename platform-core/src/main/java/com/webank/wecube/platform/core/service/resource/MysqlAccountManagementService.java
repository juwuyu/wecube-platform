package com.webank.wecube.platform.core.service.resource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.entity.plugin.ResourceItem;
import com.webank.wecube.platform.core.utils.EncryptionUtils;

@Service
public class MysqlAccountManagementService implements ResourceItemService {

    private static final Logger log = LoggerFactory.getLogger(MysqlAccountManagementService.class);

    @Autowired
    private ResourceProperties resourceProperties;

    public DriverManagerDataSource newMysqlDatasource(String host, String port, String username, String password) {
        return newMysqlDatasource(host, port, username, password, null);
    }

    public DriverManagerDataSource newMysqlDatasource(String host, String port, String username, String password,
            String database) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        if (Strings.isNullOrEmpty(database)) {
            dataSource.setUrl("jdbc:mysql://" + host + ":" + port
                    + "?characterEncoding=utf8&serverTimezone=UTC&nullCatalogMeansCurrent=true");
        } else {
            dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?characterEncoding=utf8&serverTimezone=UTC&nullCatalogMeansCurrent=true");
        }
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Override
    public ResourceItem createItem(ResourceItem item) {
        Map<String, String> additionalProperties = item.getAdditionalPropertiesMap();
        String username = additionalProperties.get("username");
        String password = additionalProperties.get("password");
        if (username == null || password == null) {
            throw new WecubeCoreException("3240", "Username or password is missing");
        }

        DriverManagerDataSource dataSource = newDatasource(item);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();) {
//            log.info("password before decrypt={}", password);
            String plainPassword = EncryptionUtils.decryptAesPrefixedStringForcely(
                    password,
                    resourceProperties.getPasswordEncryptionSeed(), item.getName());
            if(doesItemExist(item)) {
                deleteItem(item);
            }
            statement.executeUpdate(String.format("CREATE USER `%s` IDENTIFIED BY '%s'", username, plainPassword));
            statement.executeUpdate(String.format("GRANT ALL ON %s.* TO '%s'@'%%'", item.getName(),
                    username));
        } catch (Exception e) {
            String errorMessage = String.format("Failed to create account [username = %s]", username);
            log.error(errorMessage, e);
            throw new WecubeCoreException("3241", errorMessage, username);
        }
        return item;
    }

    @Override
    public void deleteItem(ResourceItem item) {
        DriverManagerDataSource dataSource = newDatasource(item);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();) {
            statement.executeUpdate(String.format("DROP USER %s", item.getName()));
        } catch (SQLException e) {
            String errorMessage = String.format("Failed to drop account [username = %s]", item.getName());
            log.error(errorMessage);
            throw new WecubeCoreException("3242", errorMessage, e);
        }
    }
    
    public boolean doesItemExist(ResourceItem item) {
        DriverManagerDataSource dataSource = newDatasource(item);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();) {
            int count = 0;
            String countUserSql = String.format("select count(1) from mysql.user where user = '%s'", item.getName()); 
            try(ResultSet rs = statement.executeQuery(countUserSql);){
                if(rs.next()) {
                    count = rs.getInt(1);
                }
            }
            
            return count > 0;
        } catch (SQLException e) {
            log.debug("Errors while checking account.", e);
            String errorMessage = String.format("Failed to check account [username = %s] : %s", item.getName(), e.getMessage());
            throw new WecubeCoreException(errorMessage);
        }
    }

    private DriverManagerDataSource newDatasource(ResourceItem item) {
        String password;
        try {
            String dbPassword = item.getResourceServer().getLoginPassword();
            
            password = EncryptionUtils.decryptAesPrefixedStringForcely(
                    dbPassword,
                    resourceProperties.getPasswordEncryptionSeed(), item.getResourceServer().getName());
        } catch (Exception e) {
            throw new WecubeCoreException("3243",
                    String.format("Failed to decrypt the login password of server [%s].", item.getResourceServer()), e);
        }

        DriverManagerDataSource dataSource = newMysqlDatasource(item.getResourceServer().getHost(),
                item.getResourceServer().getPort(), item.getResourceServer().getLoginUsername(), password);
        return dataSource;
    }

    @Override
    public ResourceItem retrieveItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceItem updateItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }
}
