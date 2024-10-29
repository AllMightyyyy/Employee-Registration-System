package org.example.workerpresence.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.workerpresence.model.Worker;
import org.example.workerpresence.model.WorkerLog;
import org.example.workerpresence.repository.WorkerLogRepository;
import org.example.workerpresence.repository.WorkerRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Controller {

    @FXML private Label workerIdLabel;
    @FXML private Label workerNameLabel;
    @FXML private Label totalHoursLabel;
    @FXML private Label statusMessage;
    @FXML private Button clockInButton;
    @FXML private Button clockOutButton;

    @FXML private TextField filterIdField;
    @FXML private DatePicker filterDatePicker;
    @FXML private TableView<WorkerLog> logTableView;
    @FXML private TableColumn<WorkerLog, Integer> colWorkerId;
    @FXML private TableColumn<WorkerLog, String> colName;
    @FXML private TableColumn<WorkerLog, LocalDateTime> colTimestamp;
    @FXML private TableColumn<WorkerLog, String> colLocation;
    @FXML private TableColumn<WorkerLog, String> colStatus;
    @FXML private Button applyFilterButton;

    private ObservableList<WorkerLog> workerLogs = FXCollections.observableArrayList();

    private WorkerRepository workerRepository = new WorkerRepository();
    private WorkerLogRepository workerLogRepository = new WorkerLogRepository();

    private Worker currentWorker;

    public void initialize() {
        colWorkerId.setCellValueFactory(new PropertyValueFactory<>("workerId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        logTableView.setItems(workerLogs);
        logTableView.refresh();

        clockInButton.setDisable(true);
        clockOutButton.setDisable(true);
    }

    @FXML
    private void handleClockIn() {
        if (currentWorker == null) {
            statusMessage.setText("No worker selected.");
            return;
        }

        List<WorkerLog> logs = workerLogRepository.getLogsByWorkerId(currentWorker.getWorkerId());
        if (!logs.isEmpty() && "IN".equalsIgnoreCase(logs.get(0).getStatus())) {
            statusMessage.setText("You are already clocked in.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        WorkerLog log = new WorkerLog(currentWorker.getWorkerId(), currentWorker.getName(),
                currentWorker.getLocation(), now, "IN");

        boolean success = workerLogRepository.addWorkerLog(log);
        if (success) {
            statusMessage.setText("Successfully clocked in at " + now.toString());
            loadWorkerLogs(currentWorker.getWorkerId());
            logTableContents();
            calculateTotalHours();
        } else {
            statusMessage.setText("Failed to clock in.");
        }
    }

    @FXML
    private void handleClockOut() {
        if (currentWorker == null) {
            statusMessage.setText("No worker selected.");
            return;
        }

        List<WorkerLog> logs = workerLogRepository.getLogsByWorkerId(currentWorker.getWorkerId());
        if (!logs.isEmpty() && "OUT".equalsIgnoreCase(logs.get(0).getStatus())) {
            statusMessage.setText("You are already clocked out.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        WorkerLog log = new WorkerLog(currentWorker.getWorkerId(), currentWorker.getName(),
                currentWorker.getLocation(), now, "OUT");

        boolean success = workerLogRepository.addWorkerLog(log);
        if (success) {
            statusMessage.setText("Successfully clocked out at " + now.toString());
            loadWorkerLogs(currentWorker.getWorkerId());
            logTableContents();
            calculateTotalHours();
        } else {
            statusMessage.setText("Failed to clock out.");
        }
    }

    @FXML
    private void handleFilter() {
        String workerIdText = filterIdField.getText().trim();
        Integer workerId = null;
        if (!workerIdText.isEmpty()) {
            try {
                workerId = Integer.parseInt(workerIdText);
            } catch (NumberFormatException e) {
                statusMessage.setText("Invalid Worker ID format.");
                return;
            }
        }

        LocalDate date = filterDatePicker.getValue();
        LocalDateTime from = null;
        LocalDateTime to = null;
        if (date != null) {
            from = date.atStartOfDay();
            to = date.plusDays(1).atStartOfDay().minusSeconds(1);
        }

        List<WorkerLog> filteredLogs = workerLogRepository.getLogsByFilter(workerId, from, to);
        workerLogs.setAll(filteredLogs);
        statusMessage.setText("Filters applied. " + filteredLogs.size() + " logs found.");
    }

    private void loadWorkerLogs(int workerId) {
        List<WorkerLog> logs = workerLogRepository.getLogsByWorkerId(workerId);
        workerLogs.setAll(logs);
        System.out.println("workerLogs size: " + workerLogs.size());
        System.out.println("Loaded " + logs.size() + " logs for Worker ID " + workerId);
    }

    private void calculateTotalHours() {
        if (currentWorker == null) {
            totalHoursLabel.setText("0:00");
            return;
        }

        List<WorkerLog> logs = workerLogRepository.getLogsByWorkerId(currentWorker.getWorkerId());
        long totalMinutes = 0;
        LocalDateTime lastClockIn = null;

        for (WorkerLog log : logs) {
            if ("IN".equalsIgnoreCase(log.getStatus())) {
                lastClockIn = log.getTimestamp();
            } else if ("OUT".equalsIgnoreCase(log.getStatus()) && lastClockIn != null) {
                long minutes = ChronoUnit.MINUTES.between(lastClockIn, log.getTimestamp());
                totalMinutes += minutes;
                lastClockIn = null;
            }
        }

        if (lastClockIn != null) {
            long minutes = ChronoUnit.MINUTES.between(lastClockIn, LocalDateTime.now());
            totalMinutes += minutes;
        }

        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        totalHoursLabel.setText(String.format("%d:%02d", hours, minutes));
    }

    public void setCurrentWorker(Worker worker) {
        this.currentWorker = worker;
        workerIdLabel.setText(String.valueOf(worker.getWorkerId()));
        workerNameLabel.setText(worker.getName());
        statusMessage.setText("Welcome, " + worker.getName() + "! Ready to Clock In/Out.");
        loadWorkerLogs(worker.getWorkerId());
        logTableContents();
        calculateTotalHours();

        clockInButton.setDisable(false);
        clockOutButton.setDisable(false);
    }

    public void logTableContents() {
        System.out.println("Logging TableView contents in User Profile:");

        for (WorkerLog log : logTableView.getItems()) {
            int workerId = log.getWorkerId();
            String name = log.getName();
            String location = log.getLocation();
            String timestamp = log.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String status = log.getStatus();

            System.out.println("Worker ID: " + workerId + ", Name: " + name +
                    ", Location: " + location + ", Timestamp: " + timestamp + ", Status: " + status);
        }
        System.out.println("End of User Profile TableView contents log.");
    }

}
