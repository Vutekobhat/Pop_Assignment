package userInterface;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignUp {
    private Stage stage;

    public SignUp(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(30));
        grid.setStyle("-fx-background-color: #f0f0f0;");

        Label titleLabel = new Label("Sign Up");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        grid.add(titleLabel, 0, 0, 2, 1);

        Label fullNameLabel = new Label("Full Name:");
        fullNameLabel.setStyle("-fx-font-size: 18px;");
        TextField fullNameField = new TextField();
        fullNameField.setPrefWidth(300);
        grid.add(fullNameLabel, 0, 1);
        grid.add(fullNameField, 1, 1);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-font-size: 18px;");
        TextField usernameField = new TextField();
        usernameField.setPrefWidth(300);
        grid.add(usernameLabel, 0, 2);
        grid.add(usernameField, 1, 2);

        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-size: 18px;");
        TextField emailField = new TextField();
        emailField.setPrefWidth(300);
        grid.add(emailLabel, 0, 3);
        grid.add(emailField, 1, 3);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-size: 18px;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPrefWidth(300);
        grid.add(passwordLabel, 0, 4);
        grid.add(passwordField, 1, 4);

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;");
        registerButton.setPrefWidth(150);
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px;");
        backButton.setPrefWidth(150);
        grid.add(registerButton, 1, 5);
        grid.add(backButton, 1, 6);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 16px;");
        grid.add(statusLabel, 1, 7);

        registerButton.setOnAction(e -> {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            if (validateInput(fullName, username, email, password)) {
                if (registerUser(fullName, username, email, password)) {
                    statusLabel.setText("Registration successful!");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    Login login = new Login(stage);
                    login.show();
                } else {
                    statusLabel.setText("Username or email already exists!");
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            } else {
                statusLabel.setText("Please fill all fields correctly!");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        backButton.setOnAction(e -> {
            Login login = new Login(stage);
            login.show();
        });

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.setTitle("Sign Up");
        stage.setMaximized(true); // Maximize the window
        stage.show();
    }

    private boolean validateInput(String fullName, String username, String email, String password) {
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return false;
        }
        if (password.length() < 6) {
            return false;
        }
        return true;
    }

    private boolean registerUser(String fullName, String username, String email, String password) {
        String sql = "INSERT INTO users (full_name, username, email, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, username);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
