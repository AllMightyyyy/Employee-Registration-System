package org.example.workerpresence.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.workerpresence.HelloApplication;
import org.example.workerpresence.model.Worker;
import org.example.workerpresence.repository.WorkerRepository;

import java.io.IOException;

public class LoginController {

    @FXML private TextField workerIdField;
    @FXML private Button loginButton;
    @FXML private Label loginStatusLabel;

    private WorkerRepository workerRepository = new WorkerRepository();

    @FXML
    private void handleLogin() {
        String workerIdText = workerIdField.getText().trim();
        if (workerIdText.isEmpty()) {
            loginStatusLabel.setText("Please enter your Worker ID.");
            return;
        }

        int workerId;
        try {
            workerId = Integer.parseInt(workerIdText);
        } catch (NumberFormatException e) {
            loginStatusLabel.setText("Invalid Worker ID format.");
            return;
        }

        Worker worker = workerRepository.getWorkerById(workerId);
        if (worker != null) {
            try {
                FXMLLoader loader;
                if ("MANAGER".equalsIgnoreCase(worker.getRole())) {
                    loader = new FXMLLoader(HelloApplication.class.getResource("manager-view.fxml"));
                } else {
                    loader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
                }

                Parent root = loader.load();

                if ("MANAGER".equalsIgnoreCase(worker.getRole())) {
                    ManagerController controller = loader.getController();
                    controller.setCurrentWorker(worker);
                } else {
                    Controller controller = loader.getController();
                    controller.setCurrentWorker(worker);
                }

                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(HelloApplication.class.getResource("styles.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Worker Presence - Dashboard");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                loginStatusLabel.setText("Failed to load main application.");
            }
        } else {
            loginStatusLabel.setText("Worker not found.");
        }
    }
}
