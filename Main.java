
package userInterface;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        Database.initializeDatabase(); //Database initialized
        Login login = new Login(primaryStage);//Login introduced
        login.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
