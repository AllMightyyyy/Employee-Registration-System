package org.example.workerpresence.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
CREATE DATABASE WorkerPresence;
CREATE TABLE workers (
    worker_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL
);
CREATE TABLE worker_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    worker_id INT NOT NULL,
    timestamp DATETIME NOT NULL,
    status ENUM('IN', 'OUT') NOT NULL,
    FOREIGN KEY (worker_id) REFERENCES workers(worker_id)
);
 */

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/WorkerPresence";
    private static final String USER = "root";
    private static final String PASSWORD = "27122000@ziko";

    private static Connection connection = null;

    // Private constructor to prevent instantiation
    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null; // Reset connection
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
