package org.example.workerpresence.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.example.workerpresence.HelloApplication;
import org.example.workerpresence.model.Worker;
import org.example.workerpresence.model.WorkerLog;
import org.example.workerpresence.repository.WorkerLogRepository;
import org.example.workerpresence.repository.WorkerRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ManagerController {

    @FXML private TextField workerIdField;
    @FXML private Button clockInButton;
    @FXML private Button clockOutButton;
    @FXML private Label managerStatusLabel;

    @FXML private TextField filterWorkerIdField;
    @FXML private DatePicker fromDatePicker;
    @FXML private Spinner<Integer> fromHourSpinner;
    @FXML private Spinner<Integer> fromMinuteSpinner;
    @FXML private DatePicker toDatePicker;
    @FXML private Spinner<Integer> toHourSpinner;
    @FXML private Spinner<Integer> toMinuteSpinner;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    @FXML private TableView<WorkerLog> logTableView;
    @FXML private TableColumn<WorkerLog, Integer> colLogId;
    @FXML private TableColumn<WorkerLog, Integer> colWorkerId;
    @FXML private TableColumn<WorkerLog, String> colName;
    @FXML private TableColumn<WorkerLog, String> colLocation;
    @FXML private TableColumn<WorkerLog, LocalDateTime> colTimestamp;
    @FXML private TableColumn<WorkerLog, String> colStatus;

    @FXML private Label clockLabel;

    private WorkerRepository workerRepository = new WorkerRepository();
    private static WorkerLogRepository workerLogRepository = new WorkerLogRepository();

    private Worker currentManager;

    private static ObservableList<WorkerLog> allLogs = FXCollections.observableArrayList();

    private Timeline clockTimeline;

    /**
     * Sets the current manager and updates the status label.
     *
     * @param worker The currently logged-in manager.
     */
    public void setCurrentWorker(Worker worker) {
        this.currentManager = worker;
        managerStatusLabel.setText("Logged in as Manager: " + worker.getName());
    }

    /**
     * Initializes the controller. Sets up table columns, initializes spinners, loads logs, and starts the clock.
     */
    @FXML
    public void initialize() {

        allLogs.add(new WorkerLog(1, 101, "Sample Name", "Sample Location", LocalDateTime.now(), "IN"));
        logTableView.setItems(allLogs);
        logTableView.refresh();


        logTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colLogId.setCellValueFactory(new PropertyValueFactory<>("logId"));
        colWorkerId.setCellValueFactory(new PropertyValueFactory<>("workerId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colTimestamp.setCellFactory(column -> new TableCell<WorkerLog, LocalDateTime>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        fromHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        fromMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        toHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 23));
        toMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 59));

        logTableView.setItems(allLogs);

        loadAllLogs();

        logTableContents();

        startClock();
    }

    /**
     * Loads all worker logs into the TableView.
     */
    void loadAllLogs() {
        List<WorkerLog> logs = workerLogRepository.getAllLogs();
        Platform.runLater(() -> {
            allLogs.setAll(logs);
            logTableView.refresh();
        });
    }

    /**
     * Handles the Clock In action for a worker.
     */
    @FXML
    private void handleClockIn() {
        String workerIdText = workerIdField.getText().trim();
        if (workerIdText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a Worker ID.");
            return;
        }

        int workerId;
        try {
            workerId = Integer.parseInt(workerIdText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Worker ID must be a number.");
            return;
        }

        Worker worker = workerRepository.getWorkerById(workerId);
        if (worker == null) {
            showAlert(Alert.AlertType.ERROR, "Worker Not Found", "No worker found with ID: " + workerId);
            return;
        }

        List<WorkerLog> logs = workerLogRepository.getLogsByWorkerId(workerId);
        if (!logs.isEmpty() && "IN".equalsIgnoreCase(logs.get(0).getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Already Clocked In", "Worker ID " + workerId + " is already clocked in.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        WorkerLog log = new WorkerLog(workerId, worker.getName(), worker.getLocation(), now, "IN");

        boolean success = workerLogRepository.addWorkerLog(log);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Clock In Successful", "Worker ID " + workerId + " clocked in successfully at " + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            loadAllLogs();
            logTableContents();
            workerIdField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Clock In Failed", "Failed to clock in Worker ID " + workerId + ".");
        }
    }

    /**
     * Handles the Clock Out action for a worker.
     */
    @FXML
    private void handleClockOut() {
        String workerIdText = workerIdField.getText().trim();
        if (workerIdText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a Worker ID.");
            return;
        }

        int workerId;
        try {
            workerId = Integer.parseInt(workerIdText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Worker ID must be a number.");
            return;
        }

        Worker worker = workerRepository.getWorkerById(workerId);
        if (worker == null) {
            showAlert(Alert.AlertType.ERROR, "Worker Not Found", "No worker found with ID: " + workerId);
            return;
        }

        List<WorkerLog> logs = workerLogRepository.getLogsByWorkerId(workerId);
        if (!logs.isEmpty() && "OUT".equalsIgnoreCase(logs.get(0).getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Already Clocked Out", "Worker ID " + workerId + " is already clocked out.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        WorkerLog log = new WorkerLog(workerId, worker.getName(), worker.getLocation(), now, "OUT");

        boolean success = workerLogRepository.addWorkerLog(log);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Clock Out Successful", "Worker ID " + workerId + " clocked out successfully at " + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            loadAllLogs();
            logTableContents();
            workerIdField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Clock Out Failed", "Failed to clock out Worker ID " + workerId + ".");
        }
    }

    /**
     * Handles the filtering of logs based on Worker ID and date-time range.
     */
    @FXML
    private void handleFilter() {
        String filterWorkerIdText = filterWorkerIdField.getText().trim();
        Integer workerId = null;
        if (!filterWorkerIdText.isEmpty()) {
            try {
                workerId = Integer.parseInt(filterWorkerIdText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Filter Worker ID must be a number.");
                return;
            }
        }

        LocalDate fromDate = fromDatePicker.getValue();
        Integer fromHour = fromHourSpinner.getValue();
        Integer fromMinute = fromMinuteSpinner.getValue();
        LocalDate toDate = toDatePicker.getValue();
        Integer toHour = toHourSpinner.getValue();
        Integer toMinute = toMinuteSpinner.getValue();

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDate != null) {
            fromDateTime = fromDate.atTime(fromHour, fromMinute);
        }

        if (toDate != null) {
            toDateTime = toDate.atTime(toHour, toMinute);
        }

        if (fromDateTime != null && toDateTime != null && fromDateTime.isAfter(toDateTime)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date Range", "'From' date-time must be before 'To' date-time.");
            return;
        }

        List<WorkerLog> filteredLogs = workerLogRepository.getLogsByFilter(workerId, fromDateTime, toDateTime);
        allLogs.setAll(filteredLogs);

        showAlert(Alert.AlertType.INFORMATION, "Filters Applied", "Logs have been filtered. " + filteredLogs.size() + " logs found.");
    }

    /**
     * Handles the clearing of applied filters and reloads all logs.
     */
    @FXML
    private void handleClearFilter() {
        filterWorkerIdField.clear();
        fromDatePicker.setValue(null);
        fromHourSpinner.getValueFactory().setValue(0);
        fromMinuteSpinner.getValueFactory().setValue(0);
        toDatePicker.setValue(null);
        toHourSpinner.getValueFactory().setValue(23);
        toMinuteSpinner.getValueFactory().setValue(59);
        loadAllLogs();
        logTableContents();
        showAlert(Alert.AlertType.INFORMATION, "Filters Cleared", "All filters have been cleared. Showing all logs.");
    }

    /**
     * Handles the logout action by returning to the login view.
     */
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) workerIdField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(HelloApplication.class.getResource("styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Worker Presence - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Logout Failed", "Failed to logout. Please try again.");
        }
    }

    /**
     * Displays an alert dialog to the user.
     *
     * @param alertType The type of alert.
     * @param title     The title of the alert.
     * @param content   The content/message of the alert.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Initializes and starts the real-time clock.
     */
    private void startClock() {
        clockTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    clockLabel.setText(LocalDateTime.now().format(formatter));
                }),
                new KeyFrame(Duration.seconds(1))
        );
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    /**
     * Logs all currently rendered values in the logTableView.
     */
    public void logTableContents() {
        System.out.println("Logging TableView contents:");

        for (WorkerLog log : logTableView.getItems()) {
            int logId = log.getLogId();
            int workerId = log.getWorkerId();
            String name = log.getName();
            String location = log.getLocation();
            String timestamp = log.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String status = log.getStatus();

            System.out.println("Log ID: " + logId + ", Worker ID: " + workerId + ", Name: " + name +
                    ", Location: " + location + ", Timestamp: " + timestamp + ", Status: " + status);
        }
        System.out.println("End of TableView contents log.");
    }

}
