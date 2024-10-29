package org.example.workerpresence.util;

import org.example.workerpresence.model.Worker;
import org.example.workerpresence.repository.WorkerRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        String createWorkersTable = "CREATE TABLE IF NOT EXISTS workers (" +
                "worker_id INT PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(100) NOT NULL," +
                "location VARCHAR(100) NOT NULL," +
                "role ENUM('WORKER', 'MANAGER') NOT NULL DEFAULT 'WORKER'" +
                ");";

        String createWorkerLogsTable = "CREATE TABLE IF NOT EXISTS worker_logs (" +
                "log_id INT PRIMARY KEY AUTO_INCREMENT," +
                "worker_id INT NOT NULL," +
                "timestamp DATETIME NOT NULL," +
                "status ENUM('IN', 'OUT') NOT NULL," +
                "FOREIGN KEY (worker_id) REFERENCES workers(worker_id)" +
                ");";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createWorkersTable);
            stmt.execute(createWorkerLogsTable);
            System.out.println("Database tables initialized successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize database tables.");
        }

        WorkerRepository workerRepository = new WorkerRepository();
        if (workerRepository.getAllWorkers().isEmpty()) {
            boolean added = workerRepository.addSampleWorkers();
            if (added) {
                System.out.println("Sample workers added.");
            } else {
                System.err.println("Failed to add sample workers.");
            }
        }
    }
}
