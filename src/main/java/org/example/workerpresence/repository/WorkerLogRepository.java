package org.example.workerpresence.repository;

import org.example.workerpresence.model.WorkerLog;
import org.example.workerpresence.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkerLogRepository {

    /**
     * Adds a new worker log to the database.
     *
     * @param log The WorkerLog object to be added.
     * @return True if the operation was successful, false otherwise.
     */
    public boolean addWorkerLog(WorkerLog log) {
        String query = "INSERT INTO worker_logs (worker_id, timestamp, status) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, log.getWorkerId());
            pstmt.setTimestamp(2, Timestamp.valueOf(log.getTimestamp()));
            pstmt.setString(3, log.getStatus());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Adding log failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    log.setLogId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Adding log failed, no ID obtained.");
                }
            }

            System.out.println("Added log: " + log);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Retrieves all worker logs from the database.
     *
     * @return A list of all WorkerLog objects.
     */
    public List<WorkerLog> getAllLogs() {
        List<WorkerLog> logs = new ArrayList<>();
        String query = "SELECT wl.log_id, wl.worker_id, w.name, w.location, wl.timestamp, wl.status " +
                "FROM worker_logs wl JOIN workers w ON wl.worker_id = w.worker_id ORDER BY wl.timestamp DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                WorkerLog log = new WorkerLog();
                log.setLogId(rs.getInt("log_id"));
                log.setWorkerId(rs.getInt("worker_id"));
                log.setName(rs.getString("name"));
                log.setLocation(rs.getString("location"));
                log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                log.setStatus(rs.getString("status"));
                logs.add(log);

                // Print each log for debugging
                System.out.println(log);
            }
            System.out.println("Retrieved " + logs.size() + " logs from the database.");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Retrieves worker logs based on provided filters.
     *
     * @param workerId      The Worker ID to filter by (nullable).
     * @param fromDateTime  The start date-time for filtering (nullable).
     * @param toDateTime    The end date-time for filtering (nullable).
     * @return A list of WorkerLog objects matching the filters.
     */
    public List<WorkerLog> getLogsByFilter(Integer workerId, LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        List<WorkerLog> logs = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT wl.log_id, wl.worker_id, w.name, w.location, wl.timestamp, wl.status " +
                        "FROM worker_logs wl JOIN workers w ON wl.worker_id = w.worker_id WHERE 1=1 "
        );

        if (workerId != null) {
            queryBuilder.append(" AND wl.worker_id = ?");
        }
        if (fromDateTime != null) {
            queryBuilder.append(" AND wl.timestamp >= ?");
        }
        if (toDateTime != null) {
            queryBuilder.append(" AND wl.timestamp <= ?");
        }
        queryBuilder.append(" ORDER BY wl.timestamp DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            int index = 1;
            if (workerId != null) {
                pstmt.setInt(index++, workerId);
            }
            if (fromDateTime != null) {
                pstmt.setTimestamp(index++, Timestamp.valueOf(fromDateTime));
            }
            if (toDateTime != null) {
                pstmt.setTimestamp(index++, Timestamp.valueOf(toDateTime));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    WorkerLog log = new WorkerLog();
                    log.setLogId(rs.getInt("log_id"));
                    log.setWorkerId(rs.getInt("worker_id"));
                    log.setName(rs.getString("name"));
                    log.setLocation(rs.getString("location"));
                    log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    log.setStatus(rs.getString("status"));
                    logs.add(log);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Retrieves worker logs by Worker ID.
     *
     * @param workerId The Worker ID to filter by.
     * @return A list of WorkerLog objects for the specified Worker ID.
     */
    public List<WorkerLog> getLogsByWorkerId(int workerId) {
        List<WorkerLog> logs = new ArrayList<>();
        String query = "SELECT wl.log_id, wl.worker_id, w.name, w.location, wl.timestamp, wl.status " +
                "FROM worker_logs wl JOIN workers w ON wl.worker_id = w.worker_id WHERE wl.worker_id = ? " +
                "ORDER BY wl.timestamp DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, workerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    WorkerLog log = new WorkerLog();
                    log.setLogId(rs.getInt("log_id"));
                    log.setWorkerId(rs.getInt("worker_id"));
                    log.setName(rs.getString("name"));
                    log.setLocation(rs.getString("location"));
                    log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    log.setStatus(rs.getString("status"));
                    logs.add(log);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }
}
