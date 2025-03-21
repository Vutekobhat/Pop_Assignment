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
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfilePage extends Application {
    private String username;

    public ProfilePage(String username) {
        this.username = username;
    }

    public ProfilePage() {
        this.username = " "; // Default for testing, more meaningful than spaces
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.setStyle("-fx-background-color: #b0b3b8;");

        Label lblTitle = new Label("User Profile");
        lblTitle.setFont(new Font("Arial", 30));
        lblTitle.setStyle("-fx-text-fill: #333333;");
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().add(lblTitle);
        grid.add(titleBox, 0, 0, 2, 1);

        Font labelFont = new Font("Times New Roman", 16);

        Label lblFullName = new Label("Full Name:");
        lblFullName.setFont(labelFont);
        lblFullName.setStyle("-fx-text-fill: #333333;");
        grid.add(lblFullName, 0, 1);

        TextField txtFullName = new TextField();
        txtFullName.setPromptText("Enter full name");
        txtFullName.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");
        grid.add(txtFullName, 1, 1);

        Label lblUsername = new Label("Username:");
        lblUsername.setFont(labelFont);
        lblUsername.setStyle("-fx-text-fill: #333333;");
        grid.add(lblUsername, 0, 2);

        // Changed from Label to TextField for consistency
        TextField txtUsername = new TextField(username);
        txtUsername.setEditable(false); // Make it non-editable
        txtUsername.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");
        grid.add(txtUsername, 1, 2);

        Label lblPhoneNumber = new Label("Phone Number:");
        lblPhoneNumber.setFont(labelFont);
        lblPhoneNumber.setStyle("-fx-text-fill: #333333;");
        grid.add(lblPhoneNumber, 0, 3);

        TextField txtPhoneNumber = new TextField();
        txtPhoneNumber.setPromptText("Enter phone number");
        txtPhoneNumber.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");
        grid.add(txtPhoneNumber, 1, 3);

        Label lblEmail = new Label("Email:");
        lblEmail.setFont(labelFont);
        lblEmail.setStyle("-fx-text-fill: #333333;");
        grid.add(lblEmail, 0, 4);

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Enter email");
        txtEmail.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");
        grid.add(txtEmail, 1, 4);

        Button btnUpdate = new Button("Update Profile");
        btnUpdate.setStyle("-fx-background-color: #95989a; -fx-text-fill: #ffffff; " +
                          "-fx-font-size: 14px; -fx-padding: 5 15 5 15;");

        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color: #95989a; -fx-text-fill: #ffffff; " +
                          "-fx-font-size: 14px; -fx-padding: 5 15 5 15;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(btnUpdate, btnLogout);
        grid.add(buttonBox, 1, 5);

        Label lblStatus = new Label("");
        grid.add(lblStatus, 1, 6);

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #95989a; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.getChildren().add(backButton);
        grid.add(bottomBar, 1, 7); // Fixed: Changed customBar to bottomBar

        // Load existing user data
        loadUserData(txtFullName, txtPhoneNumber, txtEmail);

        btnUpdate.setOnAction(e -> {
            if (validateInput(txtFullName.getText(), txtPhoneNumber.getText(), txtEmail.getText())) {
                if (updateProfile(txtFullName.getText(), txtPhoneNumber.getText(), txtEmail.getText())) {
                    lblStatus.setText("Profile updated successfully!");
                    lblStatus.setStyle("-fx-text-fill: #006400;");
                } else {
                    lblStatus.setText("Failed to update profile!");
                    lblStatus.setStyle("-fx-text-fill: #8b0000;");
                }
            } else {
                lblStatus.setText("Please fill all fields correctly");
                lblStatus.setStyle("-fx-text-fill: #8b0000;");
            }
        });

        btnLogout.setOnAction(e -> {
            lblStatus.setText("Logged out successfully");
            lblStatus.setStyle("-fx-text-fill: #006400;");
            Login login = new Login();
            try {
                login.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        backButton.setOnAction(e -> {
            Dashboard dashboard = new Dashboard(primaryStage, username);
            primaryStage.getScene().setRoot(dashboard.getView());
        });

        Scene scene = new Scene(grid, 500, 450);
        primaryStage.setScene(scene);
        primaryStage.setTitle("User Profile");
        primaryStage.show();
    }

    private boolean validateInput(String fullName, String phone, String email) {
        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            return false;
        }
        if (!phone.matches("\\d+")) {
            return false;
        }
        return true;
    }

    private void loadUserData(TextField txtFullName, TextField txtPhoneNumber, TextField txtEmail) {
        String sql = "SELECT full_name, phone_number, email FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                txtFullName.setText(rs.getString("full_name") != null ? rs.getString("full_name") : "");
                txtPhoneNumber.setText(rs.getString("phone_number") != null ? rs.getString("phone_number") : "");
                txtEmail.setText(rs.getString("email") != null ? rs.getString("email") : "");
            } else {
                // Default to empty strings if no data found
                txtFullName.setText("");
                txtPhoneNumber.setText("");
                txtEmail.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Fallback to empty strings on error
            txtFullName.setText("");
            txtPhoneNumber.setText("");
            txtEmail.setText("");
        }
    }

    private boolean updateProfile(String fullName, String phoneNumber, String email) {
        String sql = "UPDATE users SET full_name = ?, phone_number = ?, email = ? WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, phoneNumber);
            stmt.setString(3, email);
            stmt.setString(4, username);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}