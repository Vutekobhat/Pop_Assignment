package userInterface;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class SignUp extends Application {

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = createGridPane();
        setupUIComponents(grid, primaryStage);

        Scene scene = new Scene(grid, 500, 420); // Slightly increased height for back button
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sign Up"); // name of the window
        primaryStage.setResizable(false); // not resize-able
        primaryStage.show();
    }

    private GridPane createGridPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.setStyle("-fx-background-color: #b0b3b8;");
        return grid;
    }

    private void setupUIComponents(GridPane grid, Stage primaryStage) {
        // Title
        Label lblTitle = new Label("Sign Up");
        lblTitle.setFont(new Font("Arial", 30)); // Arial font with size 30
        lblTitle.setStyle("-fx-text-fill: #333333;"); // text color
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().add(lblTitle);
        grid.add(titleBox, 0, 0, 2, 1);

        Font labelFont = new Font("Times New Roman", 16);

        // Input fields
        TextField txtFullName = createTextField(grid, "Full Name:", 1, labelFont);
        TextField txtUsername = createTextField(grid, "Username:", 2, labelFont);
        TextField txtPhoneNumber = createTextField(grid, "Phone Number:", 3, labelFont);
        TextField txtEmail = createTextField(grid, "Email:", 4, labelFont);
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Enter password");
        txtPassword.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setStyle("-fx-text-fill: #333333;");
        grid.add(passwordLabel, 0, 5);
        grid.add(txtPassword, 1, 5);

        // Buttons
        Button btnRegister = createButton("Register");
        Button btnClear = createButton("Clear");
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(btnRegister, btnClear);
        grid.add(buttonBox, 1, 6);

        // Status label
        Label lblStatus = new Label("");
        grid.add(lblStatus, 1, 7);

        // Back Button
        Button backButton = createButton("Back");
        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.getChildren().add(backButton);
        grid.add(bottomBar, 1, 8); // Fixed: Changed customBar to bottomBar

        // Event handlers
        setupEventHandlers(btnRegister, btnClear, backButton, txtFullName, txtUsername,
                          txtPhoneNumber, txtEmail, txtPassword, lblStatus, primaryStage);
    }

    private TextField createTextField(GridPane grid, String labelText, int row, Font font) {
        Label label = new Label(labelText);
        label.setFont(font);
        label.setStyle("-fx-text-fill: #333333;");
        TextField textField = new TextField();
        textField.setPromptText("Enter " + labelText.toLowerCase().replace(":", ""));
        textField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");
        grid.add(label, 0, row);
        grid.add(textField, 1, row);
        return textField;
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #95989a; -fx-text-fill: #ffffff; " +
                       "-fx-font-size: 14px; -fx-padding: 5 15 5 15;");
        return button;
    }

    private void setupEventHandlers(Button btnRegister, Button btnClear, Button backButton,
                                   TextField txtFullName, TextField txtUsername,
                                   TextField txtPhoneNumber, TextField txtEmail,
                                   PasswordField txtPassword, Label lblStatus, Stage primaryStage) {
        btnRegister.setOnAction(e -> {
            String fullName = txtFullName.getText().trim();
            String username = txtUsername.getText().trim();
            String phone = txtPhoneNumber.getText().trim();
            String email = txtEmail.getText().trim();
            String password = txtPassword.getText();

            if (validateInput(fullName, username, phone, email, password)) {
                try {
                    if (registerUser(fullName, username, phone, email, password)) {
                        lblStatus.setText("Registration successful!");
                        lblStatus.setStyle("-fx-text-fill: #006400;");
                        Login login = new Login();
                        login.start(primaryStage);
                    } else {
                        lblStatus.setText("Registration failed (duplicate username/email)");
                        lblStatus.setStyle("-fx-text-fill: #8b0000;");
                    }
                } catch (SQLException ex) {
                    lblStatus.setText("Database error: " + ex.getMessage());
                    lblStatus.setStyle("-fx-text-fill: #8b0000;");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                lblStatus.setText("Please fill all fields correctly");
                lblStatus.setStyle("-fx-text-fill: #8b0000;");
            }
        });

        btnClear.setOnAction(e -> {
            txtFullName.clear();
            txtUsername.clear();
            txtPhoneNumber.clear();
            txtEmail.clear();
            txtPassword.clear();
            lblStatus.setText("");
        });

        backButton.setOnAction(e -> {
            Login login = new Login();
            try {
                login.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private boolean validateInput(String fullName, String username, String phone,
                                 String email, String password) {
        if (fullName.isEmpty() || username.isEmpty() || phone.isEmpty() ||
            email.isEmpty() || password.isEmpty()) {
            return false;
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.matches(emailRegex, email)) {
            return false;
        }

        if (!phone.matches("\\d{10}")) {
            return false;
        }

        if (password.length() < 6) {
            return false;
        }

        return true;
    }

    private boolean registerUser(String fullName, String username, String phone,
                                String email, String password) throws SQLException {
        String sql = "INSERT INTO users (full_name, username, phone_number, email, password) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, username);
            stmt.setString(3, phone);
            stmt.setString(4, email);
            // TODO: In production, use password hashing (e.g., BCrypt)
            stmt.setString(5, password);
            return stmt.executeUpdate() > 0;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}