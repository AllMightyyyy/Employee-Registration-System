package org.example.workerpresence.repository;

import org.example.workerpresence.model.Worker;
import org.example.workerpresence.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkerRepository {

    public Worker getWorkerById(int workerId) {
        String query = "SELECT * FROM workers WHERE worker_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, workerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Worker worker = new Worker();
                worker.setWorkerId(rs.getInt("worker_id"));
                worker.setName(rs.getString("name"));
                worker.setLocation(rs.getString("location"));
                worker.setRole(rs.getString("role"));
                return worker;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addWorker(Worker worker) {
        String query = "INSERT INTO workers (name, location, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, worker.getName());
            pstmt.setString(2, worker.getLocation());
            pstmt.setString(3, worker.getRole());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Adding worker failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    worker.setWorkerId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Adding worker failed, no ID obtained.");
                }
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateWorker(Worker worker) {
        String query = "UPDATE workers SET name = ?, location = ? WHERE worker_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, worker.getName());
            pstmt.setString(2, worker.getLocation());
            pstmt.setInt(3, worker.getWorkerId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Worker> getAllWorkers() {
        List<Worker> workers = new ArrayList<>();
        String query = "SELECT * FROM workers";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Worker worker = new Worker();
                worker.setWorkerId(rs.getInt("worker_id"));
                worker.setName(rs.getString("name"));
                worker.setLocation(rs.getString("location"));
                workers.add(worker);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return workers;
    }

    public boolean addSampleWorkers() {
        Worker worker1 = new Worker();
        worker1.setName("Alice Johnson");
        worker1.setLocation("New York");
        worker1.setRole("WORKER");

        Worker worker2 = new Worker();
        worker2.setName("Bob Smith");
        worker2.setLocation("Los Angeles");
        worker2.setRole("MANAGER");

        return addWorker(worker1) && addWorker(worker2);
    }
}
