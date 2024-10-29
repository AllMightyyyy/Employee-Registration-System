package org.example.workerpresence.model;

public class Worker {
    private int workerId;
    private String name;
    private String location;
    private String role;

    public Worker() {}

    public Worker(int workerId, String name, String location) {
        this.workerId = workerId;
        this.name = name;
        this.location = location;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
