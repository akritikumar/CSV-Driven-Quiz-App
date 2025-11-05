module com.example.csvdriven_quiz_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.csvdriven_quiz_app to javafx.fxml;
    exports com.example.csvdriven_quiz_app;

    opens quiz to javafx.fxml;
    exports quiz;
}