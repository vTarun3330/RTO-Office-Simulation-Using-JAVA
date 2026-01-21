package com.rto;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        setRoot("LoginView");
        stage.setTitle("RTO Management System");
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/rto/view/" + fxml + ".fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600); // Standard Dashboard Size
        primaryStage.setScene(scene);
    }

    public static Stage getStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        // Initialize Core System Facade or Patterns if needed here
        launch();
    }
}
