package com.webank.wecube.platform.core.service.resource;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.dto.plugin.PageInfo;
import com.webank.wecube.platform.core.dto.plugin.QueryResponse;
import com.webank.wecube.platform.core.dto.plugin.SqlQueryRequest;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.entity.plugin.PluginInstances;
import com.webank.wecube.platform.core.entity.plugin.PluginMysqlInstances;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.ResourceItem;
import com.webank.wecube.platform.core.entity.plugin.ResourceServer;
import com.webank.wecube.platform.core.repository.plugin.PluginInstancesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginMysqlInstancesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.core.repository.plugin.ResourceItemMapper;
import com.webank.wecube.platform.core.repository.plugin.ResourceServerMapper;
import com.webank.wecube.platform.core.support.S3Client;
import com.webank.wecube.platform.core.utils.EncryptionUtils;

@Service
public class ResourceDataQueryService {
    private Logger logger = LoggerFactory.getLogger(ResourceDataQueryService.class);

    private static final List<String> ALLOWED_SQL_FUNCTIONS = Arrays.asList("count", "avg", "sum", "min", "max",
            "distinct");
    private static final String[] JDBC_METADATA_TABLE_TYPES = { "TABLE" };

    @Autowired
    private PluginMysqlInstancesMapper pluginMysqlInstancesMapper;
    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;
    @Autowired
    private PluginInstancesMapper pluginInstancesMapper;
    @Autowired
    private ResourceItemMapper resourceItemMapper;
    @Autowired
    private ResourceServerMapper resourceServerMapper;
    @Autowired
    private ResourceProperties resourceProperties;
    @Autowired
    private MysqlAccountManagementService mysqlAcctMngService;

    @Autowired
    private S3Client s3client;

    public QueryResponse<List<String>> queryDB(String packageId, SqlQueryRequest sqlQueryRequest) {
        DataSource dataSource = getDataSource(packageId);
        return queryDB(dataSource, sqlQueryRequest);
    }

    public QueryResponse<List<String>> queryDB(DataSource dataSource, SqlQueryRequest sqlQueryRequest) {
        List<List<String>> results = new LinkedList<>();

        sqlInjectionValidation(sqlQueryRequest.getSqlQuery(), dataSource);

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();) {
            int totalCount = queryTotalCount(statement, sqlQueryRequest.getSqlQuery());

            String limitedSql = getLimitedSql(sqlQueryRequest);
            ResultSet rs = statement.executeQuery(limitedSql);
            ResultSetMetaData resultSetMd = rs.getMetaData();
            int columnCount = resultSetMd.getColumnCount();

            List<String> headers = getHeaders(resultSetMd);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            while (rs.next()) {
                List<String> rowValue = new ArrayList<>(columnCount);
                for (int col = 1; col <= columnCount; col++) {
                    int colType = resultSetMd.getColumnType(col);
                    String literalVal = null;
                    switch (colType) {
                    case Types.DATE:
                        Date date = rs.getDate(col);
                        if (date != null) {
                            literalVal = dateFormat.format(date);
                        }
                        break;
                    case Types.TIMESTAMP:
                        Timestamp timestamp = rs.getTimestamp(col);
                        if (timestamp != null) {
                            literalVal = datetimeFormat.format(timestamp);
                        }
                        break;
                    default:
                        literalVal = rs.getString(col);
                        break;
                    }
                    rowValue.add(literalVal);
                }
                results.add(rowValue);
            }
            QueryResponse<List<String>> response = null;
            if (sqlQueryRequest.getPageable() != null) {
                response = new QueryResponse<>(new PageInfo(totalCount, sqlQueryRequest.getPageable().getStartIndex(),
                        sqlQueryRequest.getPageable().getPageSize()), results, headers);
            } else {
                response = new QueryResponse<>(new PageInfo(totalCount, 0, totalCount), results, headers);
            }
            return response;

        } catch (Exception e) {
            String errorMessage = String.format("Fail to execute sql query:%s", sqlQueryRequest.toString());
            logger.error(errorMessage, e);
            throw new WecubeCoreException(errorMessage, e).withErrorCode("3010", sqlQueryRequest.toString());
        }
    }

    private void sqlInjectionValidation(String rawSql, DataSource dataSource) {
        if (StringUtils.isBlank(rawSql)) {
            return;
        }

        String formattedSql = rawSql.trim().replaceAll("\\\\'", "ESCAPE");

        formattedSql = formattedSql.replaceAll("'[^']*'", "?");

        if (formattedSql.contains("'")) {
            throw new WecubeCoreException("Single quote must be paired.").withErrorCode("3313");
        }

        formattedSql = formattedSql.toLowerCase();
        if (formattedSql.contains("union")) {
            throw new WecubeCoreException("Union query is not allowed.").withErrorCode("3314");
        }

        if (formattedSql.contains(";")) {
            throw new WecubeCoreException("Semicolon is not allowed.").withErrorCode("3315");
        }

        validateSubSelection(formattedSql);
        validateFunctions(formattedSql);
        validateTableNames(formattedSql, dataSource);
    }

    private void validateSubSelection(String formattedSql) {
        String selectCauseReg = "(\\W+)select(\\W+)";
        Pattern selectp = Pattern.compile(selectCauseReg);
        Matcher selectm = selectp.matcher(formattedSql);
        if (selectm.find()) {
            throw new WecubeCoreException("Sub-selection is not allowed.").withErrorCode("3316");
        }
    }

    private void validateTableNames(String formattedSql, DataSource dataSource) {
        formattedSql = formattedSql.trim() + " ";
        String tableNameReg = "\\s+(from|join)\\s+(\\S+?)[\\s|\\)]+";
        Pattern tableNameP = Pattern.compile(tableNameReg);
        Matcher tableNameM = tableNameP.matcher(formattedSql);
        Set<String> inputTableNames = new HashSet<String>();
        while (tableNameM.find()) {
            inputTableNames.add(tableNameM.group(2));
        }

        if (inputTableNames.isEmpty()) {
            logger.info("None table names found in input SQL:{}", formattedSql);
            return;
        }

        Set<String> tableNames = new HashSet<>();
        ResultSet tablesRs = null;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            String jdbcUrl = connection.getMetaData().getURL().toString();
            String strSchema = null;
            if (jdbcUrl.indexOf("?") > 0 && jdbcUrl.indexOf("/") > 0) {
                strSchema = jdbcUrl.substring(0, jdbcUrl.indexOf("?"));
                strSchema = strSchema.substring(strSchema.lastIndexOf("/") + 1);
            }

            DatabaseMetaData databaseMetaData = connection.getMetaData();
            tablesRs = databaseMetaData.getTables(null, strSchema, null, JDBC_METADATA_TABLE_TYPES);
            while (tablesRs.next()) {
                String tableName = tablesRs.getString("TABLE_NAME");
                tableName = tableName.trim().toLowerCase();
                tableNames.add(tableName);
            }
        } catch (SQLException e) {
            logger.error("", e);
            throw new WecubeCoreException("Errors while fetching table names").withErrorCode("3317");
        } finally {
            closeSilently(tablesRs);
            closeSilently(connection);
        }

        for (String inputTabName : inputTableNames) {
            if (!tableNames.contains(inputTabName)) {
                throw new WecubeCoreException(String.format("Selection to %s is not allowed.", inputTabName))
                        .withErrorCode("3318", inputTabName);
            }
        }
    }

    private void validateFunctions(String formattedSql) {
        String functionNameReg = "(\\W+?)(\\w+?)\\(.*?\\)";
        Pattern p = Pattern.compile(functionNameReg);
        Matcher m = p.matcher(formattedSql);

        while (m.find()) {
            String funName = m.group(2);
            if (!ALLOWED_SQL_FUNCTIONS.contains(funName)) {
                throw new WecubeCoreException(String.format("Such function [%s] is not allowed.", funName))
                        .withErrorCode("3319", funName);
            }
        }
    }

    private List<String> getHeaders(ResultSetMetaData resultSetMd) throws SQLException {
        List<String> headers = new ArrayList<>(resultSetMd.getColumnCount());
        for (int i = 1; i <= resultSetMd.getColumnCount(); i++) {
            headers.add(resultSetMd.getColumnLabel(i));
        }
        return headers;
    }

    private String getLimitedSql(SqlQueryRequest sqlQueryRequest) {
        if (sqlQueryRequest.getPageable() != null) {
            int startIndex = sqlQueryRequest.getPageable().getStartIndex();
            int pageSize = sqlQueryRequest.getPageable().getPageSize();
            StringBuilder limitedSqlBuilder = new StringBuilder();
            limitedSqlBuilder.append("select * from (").append(sqlQueryRequest.getSqlQuery()).append(") alias limit ")
                    .append(startIndex).append(",").append(pageSize);
            return limitedSqlBuilder.toString();
        } else {
            return sqlQueryRequest.getSqlQuery();
        }
    }

    private int queryTotalCount(Statement statement, String sqlQuery) {
        StringBuilder countSqlBuilder = new StringBuilder();
        countSqlBuilder.append("select count(1) from ").append("(").append(sqlQuery).append(") alias");
        try (ResultSet rs = statement.executeQuery(countSqlBuilder.toString())) {
            if (rs.first()) {
                int totalCount = rs.getInt(1);
                return totalCount;
            } else {
                throw new WecubeCoreException("3011", String.format("Failed to get total count of query: %s", sqlQuery),
                        sqlQuery);
            }
        } catch (Exception ex) {
            throw new WecubeCoreException("3011", String.format("Failed to get total count of query: %s", sqlQuery),
                    sqlQuery);
        }
    }

    private DataSource getDataSource(String packageId) {
        PluginPackages pluginPackage = pluginPackagesMapper.selectByPrimaryKey(packageId);
        if (pluginPackage == null) {
            String errMsg = String.format("Can not find out PluginPackage for package id:%s", packageId);
            throw new WecubeCoreException("3012", errMsg, packageId);
        }

        List<PluginMysqlInstances> pluginMysqlInstances = pluginMysqlInstancesMapper.selectAllByPackageNameAndStatus(
                pluginPackage.getName(), PluginMysqlInstances.MYSQL_INSTANCE_STATUS_ACTIVE);

        if (pluginMysqlInstances == null || pluginMysqlInstances.isEmpty()) {
            String errMsg = String.format("Can not find out PluginMysqlInstance for package name:%s",
                    pluginPackage.getName());
            throw new WecubeCoreException("3013", errMsg, pluginPackage.getName());
        }

        PluginMysqlInstances pluginMysqlInstance = pluginMysqlInstances.get(0);

        String dbUsername = pluginMysqlInstance.getUsername();
        String password = pluginMysqlInstance.getPassword();

        password = EncryptionUtils.decryptAesPrefixedStringForcely(password, resourceProperties.getPasswordEncryptionSeed(), dbUsername);

        ResourceItem resourceItem = resourceItemMapper.selectByPrimaryKey(pluginMysqlInstance.getResourceItemId());
        if (resourceItem == null) {
            throw new WecubeCoreException("3014",
                    String.format("Can not find out ResourceItem for packageId:%s", packageId), packageId);
        }

        
        ResourceServer resourceServer = resourceServerMapper.selectByPrimaryKey(resourceItem.getResourceServerId());
        if (resourceServer == null) {
            throw new WecubeCoreException("3015",
                    String.format("Can not find out mysql ResourceServer for packageId:%s", packageId), packageId);
        }

        String mysqlHost = resourceServer.getHost();
        String mysqlPort = resourceServer.getPort();
        return mysqlAcctMngService.newMysqlDatasource(mysqlHost, mysqlPort, dbUsername, password,
                pluginMysqlInstance.getSchemaName());
    }

    public List<List<String>> queryS3Files(String packageId) {
        List<PluginInstances> pluginInstances = pluginInstancesMapper.selectAllByPluginPackage(packageId);
        if (pluginInstances == null || pluginInstances.isEmpty()) {
            logger.info("Can not find out plugin instance for packageId:{}", packageId);
            return Lists.newArrayList();
        }

        String bucketName = null;
        for (PluginInstances ps : pluginInstances) {
            if (StringUtils.isBlank(ps.getS3bucketResourceId())) {
                continue;
            }

            ResourceItem item = resourceItemMapper.selectByPrimaryKey(ps.getS3bucketResourceId());
            if (item != null) {
                bucketName = item.getName();
                break;
            }
        }

        if (Strings.isNullOrEmpty(bucketName)) {
            return Lists.newArrayList();
        }

        List<S3ObjectSummary> s3Objs = s3client.listObjects(bucketName);
        List<List<String>> response = new LinkedList<>();
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (S3ObjectSummary s3ObjSum : s3Objs) {
            List<String> rowVal = new ArrayList<>(4);
            String key = s3ObjSum.getKey();
            int lastSplitPos = key.lastIndexOf("/");
            String path = "";
            String fileName = "";
            if (lastSplitPos > 0) {
                path = key.substring(0, lastSplitPos + 1);
                fileName = key.substring(lastSplitPos + 1);
            } else {
                path = "/";
                fileName = key;
            }
            rowVal.add(fileName);
            rowVal.add(path);
            rowVal.add(s3ObjSum.getETag());
            rowVal.add(datetimeFormat.format(s3ObjSum.getLastModified()));
            response.add(rowVal);
        }
        return response;
    }

    protected void closeSilently(AutoCloseable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    protected void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                logger.error("", e);
            }
        }
    }

}
