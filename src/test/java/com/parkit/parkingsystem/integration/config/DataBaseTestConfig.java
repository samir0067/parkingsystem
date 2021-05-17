package com.parkit.parkingsystem.integration.config;

import com.parkit.parkingsystem.config.DataBaseConfig;
import org.slf4j.Logger;

import java.sql.*;

import static org.slf4j.LoggerFactory.getLogger;

public class DataBaseTestConfig extends DataBaseConfig {

    private static final Logger LOGGER = getLogger(DataBaseTestConfig.class);

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        LOGGER.info("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/test?serverTimezone=UTC", "Samir", "ihsane11");
    }

    public void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
                LOGGER.info("Closing DB connection !!!");
            } catch (SQLException e) {
                LOGGER.error("Error while closing connection ===>", e);
            }
        }
    }

    public void closePreparedStatement(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
                LOGGER.info("Closing Prepared Statement !!!");
            } catch (SQLException e) {
                LOGGER.error("Error while closing prepared statement ===>", e);
            }
        }
    }

    public void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                LOGGER.info("Closing Result Set !!!");
            } catch (SQLException e) {
                LOGGER.error("Error while closing result set ===>", e);
            }
        }
    }
}
