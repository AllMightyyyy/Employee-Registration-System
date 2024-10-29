package org.example.workerpresence.model;

import java.time.LocalDateTime;

public class WorkerLog {
    private int logId;
    private int workerId;
    private String name;
    private String location;
    private LocalDateTime timestamp;
    private String status;

    public WorkerLog() {}

    public WorkerLog(int logId, int workerId, String name, String location, LocalDateTime timestamp, String status) {
        this.logId = logId;
        this.workerId = workerId;
        this.name = name;
        this.location = location;
        this.timestamp = timestamp;
        this.status = status;
    }

    public WorkerLog(int workerId, String name, String location, LocalDateTime timestamp, String status) {
        this.workerId = workerId;
        this.name = name;
        this.location = location;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and Setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "WorkerLog{" +
                "logId=" + logId +
                ", workerId=" + workerId +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}
