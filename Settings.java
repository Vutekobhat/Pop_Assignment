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

public class Settings {
    private Stage stage;
    private String username;

    public Settings(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }
    //Window alignment
    public void show() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(30));
        grid.setStyle("-fx-background-color: #f0f0f0;");

        //Label and title
        Label titleLabel = new Label("Settings");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        grid.add(titleLabel, 0, 0, 2, 1);

        Label currentPasswordLabel = new Label("Current Password:");
        currentPasswordLabel.setStyle("-fx-font-size: 18px;");
        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPrefWidth(300);
        currentPasswordField.setStyle("-fx-font-size: 16px;");
        grid.add(currentPasswordLabel, 0, 1);
        grid.add(currentPasswordField, 1, 1);

        Label newPasswordLabel = new Label("New Password:");
        newPasswordLabel.setStyle("-fx-font-size: 18px;");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPrefWidth(300);
        newPasswordField.setStyle("-fx-font-size: 16px;");
        grid.add(newPasswordLabel, 0, 2);
        grid.add(newPasswordField, 1, 2);

        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.setStyle("-fx-font-size: 18px;");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPrefWidth(300);
        confirmPasswordField.setStyle("-fx-font-size: 16px;");
        grid.add(confirmPasswordLabel, 0, 3);
        grid.add(confirmPasswordField, 1, 3);

        // Adding expense limit input field
        Label expenseLimitLabel = new Label("Monthly Expense Limit:");
        expenseLimitLabel.setStyle("-fx-font-size: 18px;");
        TextField expenseLimitField = new TextField();
        expenseLimitField.setPromptText("Enter limit (e.g., 500.00)");
        expenseLimitField.setPrefWidth(300);
        expenseLimitField.setStyle("-fx-font-size: 16px;");
        grid.add(expenseLimitLabel, 0, 4);
        grid.add(expenseLimitField, 1, 4);

        //Button
        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;");
        changePasswordButton.setPrefWidth(150);
        Button setLimitButton = new Button("Set Limit");
        setLimitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;");
        setLimitButton.setPrefWidth(150);
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px;");
        backButton.setPrefWidth(150);
        grid.add(changePasswordButton, 1, 5);
        grid.add(setLimitButton, 1, 6);
        grid.add(backButton, 1, 7);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 16px;");
        grid.add(statusLabel, 1, 8);

        // Load the current expense limit (if set)
        loadExpenseLimit(expenseLimitField);

        //Giving action to buttons
        changePasswordButton.setOnAction(e -> {
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                statusLabel.setText("Please fill all fields!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                statusLabel.setText("New passwords do not match!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            if (newPassword.length() < 6) {
                statusLabel.setText("New password must be at least 6 characters!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            if (changePassword(currentPassword, newPassword)) {
                statusLabel.setText("Password changed successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            } else {
                statusLabel.setText("Current password incorrect!");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        setLimitButton.setOnAction(e -> {
            String limitText = expenseLimitField.getText().trim();
            Double limit = null;
            if (!limitText.isEmpty()) {
                try {
                    limit = Double.parseDouble(limitText);
                    if (limit <= 0) {
                        statusLabel.setText("Limit must be positive!");
                        statusLabel.setStyle("-fx-text-fill: red;");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    statusLabel.setText("Invalid limit value!");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    return;
                }
            }
            updateExpenseLimit(limit);
            statusLabel.setText("Expense limit updated!");
            statusLabel.setStyle("-fx-text-fill: green;");
        });

        backButton.setOnAction(e -> {
            Dashboard dashboard = new Dashboard(stage, username);
            dashboard.show();
        });

    
        
        //Scene
        Scene scene = new Scene(grid);
        // Load stylesheet with error handling
        String stylesheet = getClass().getResource("/resources/styles.css") != null ? 
                           getClass().getResource("/resources/styles.css").toExternalForm() : null;
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet);
        } else {
            System.err.println("Warning: styles.css not found in /resources. Proceeding without stylesheet.");
        }
        stage.setScene(scene);
        stage.setTitle("Settings - " + username);
        stage.setMaximized(true);
        stage.show();
    }

    private void loadExpenseLimit(TextField expenseLimitField) {
        String sql = "SELECT expense_limit FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double limit = rs.getDouble("expense_limit");
                if (!rs.wasNull()) {
                    expenseLimitField.setText(String.format("%.2f", limit));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Expense limit
    private void updateExpenseLimit(Double limit) {
        String sql = "UPDATE users SET expense_limit = ? WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (limit == null) {
                stmt.setNull(1, java.sql.Types.DOUBLE);
            } else {
                stmt.setDouble(1, limit);
            }
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Changing password
    private boolean changePassword(String currentPassword, String newPassword) {
        String sqlCheck = "SELECT password FROM users WHERE username = ? AND password = ?";
        String sqlUpdate = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = Database.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement(sqlCheck);
            checkStmt.setString(1, username);
            checkStmt.setString(2, currentPassword);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                return false;
            }

            PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate);
            updateStmt.setString(1, newPassword);
            updateStmt.setString(2, username);
            return updateStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}