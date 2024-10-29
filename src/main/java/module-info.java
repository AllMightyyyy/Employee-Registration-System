module org.example.workerpresence {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.workerpresence to javafx.fxml;
    opens org.example.workerpresence.model to javafx.base, javafx.fxml;
    exports org.example.workerpresence;
    exports org.example.workerpresence.controller;
    opens org.example.workerpresence.controller to javafx.fxml;
}