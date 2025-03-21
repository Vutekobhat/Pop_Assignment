package userInterface;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Suppress macOS-specific JavaFX logs (optional)
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "ExpenseManagement");

        Login login = new Login();
        try {
            login.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}