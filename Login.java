 package userInterface;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {
    private Stage stage;

    public Login(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15); // Increased spacing for larger screen
        grid.setVgap(15);
        grid.setPadding(new Insets(30)); // Increased padding
        grid.setStyle("-fx-background-color: #f0f0f0;");

        Label titleLabel = new Label("Login");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;"); // Increased font size
        grid.add(titleLabel, 0, 0, 2, 1);

        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-size: 18px;"); // Increased font size
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");
        emailField.setPrefWidth(300); // Increased width for larger screen
        grid.add(emailLabel, 0, 1);
        grid.add(emailField, 1, 1);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-size: 18px;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefWidth(300);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;");
        loginButton.setPrefWidth(150); // Increased button size
        Button signupButton = new Button("Sign Up");
        signupButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px;");
        signupButton.setPrefWidth(150);
        grid.add(loginButton, 1, 3);
        grid.add(signupButton, 1, 4);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 16px;");
        grid.add(statusLabel, 1, 5);

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            if (email.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please fill all fields");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            String username = authenticate(email, password);
            if (username != null) {
                Dashboard dashboard = new Dashboard(stage, username);
                dashboard.show();
            } else {
                statusLabel.setText("Invalid email or password");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        signupButton.setOnAction(e -> {
            SignUp signUp = new SignUp(stage);
            signUp.show();
        });

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.setMaximized(true); // Maximize the window
        stage.show();
    }

    private String authenticate(String email, String password) {
        String sql = "SELECT username FROM users WHERE email = ? AND password = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}                                          
