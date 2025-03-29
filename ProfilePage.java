package userInterface;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfilePage {
    private Stage stage;
    private String username;
    private ImageView profileImageView;

    public ProfilePage(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }

    public void show() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(30));

        Label titleLabel = new Label("Profile");
        titleLabel.getStyleClass().add("title-label");
        grid.add(titleLabel, 0, 0, 2, 1);

        profileImageView = new ImageView();
        profileImageView.setFitHeight(150);
        profileImageView.setFitWidth(150);
        Button uploadButton = new Button("Upload Picture");
        uploadButton.getStyleClass().addAll("button", "blue-button");
        grid.add(profileImageView, 0, 1);
        grid.add(uploadButton, 1, 1);

        Label fullNameLabel = new Label("Full Name:");
        fullNameLabel.getStyleClass().add("label");
        TextField fullNameField = new TextField();
        fullNameField.getStyleClass().add("text-field");
        grid.add(fullNameLabel, 0, 2);
        grid.add(fullNameField, 1, 2);

        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().add("label");
        TextField emailField = new TextField();
        emailField.getStyleClass().add("text-field");
        grid.add(emailLabel, 0, 3);
        grid.add(emailField, 1, 3);

        Label phoneLabel = new Label("Phone:");
        phoneLabel.getStyleClass().add("label");
        TextField phoneField = new TextField();
        phoneField.getStyleClass().add("text-field");
        grid.add(phoneLabel, 0, 4);
        grid.add(phoneField, 1, 4);

        Button updateButton = new Button("Update");
        updateButton.getStyleClass().addAll("button", "green-button");
        Button backButton = new Button("Back");
        backButton.getStyleClass().addAll("button", "blue-button");
        grid.add(updateButton, 1, 5);
        grid.add(backButton, 1, 6);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");
        grid.add(statusLabel, 1, 7);

        loadUserData(fullNameField, emailField, phoneField);

        uploadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Picture");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                profileImageView.setImage(image);
                updateProfilePicture(file.getAbsolutePath());
                statusLabel.setText("Profile picture updated!");
                statusLabel.setStyle("-fx-text-fill: green;");
            }
        });

        updateButton.setOnAction(e -> {
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            if (validateInput(fullName, email, phone)) {
                if (updateProfile(fullName, email, phone)) {
                    statusLabel.setText("Profile updated!");
                    statusLabel.setStyle("-fx-text-fill: green;");
                } else {
                    statusLabel.setText("Update failed!");
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            } else {
                statusLabel.setText("Please fill all fields correctly!");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        backButton.setOnAction(e -> {
            Dashboard dashboard = new Dashboard(stage, username);
            dashboard.show();
        });

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
        stage.setTitle("Profile - " + username);
        stage.setMaximized(true);
        stage.show();
    }

    private boolean validateInput(String fullName, String email, String phone) {
        if (fullName.isEmpty() || email.isEmpty()) {
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return false;
        }
        if (!phone.isEmpty() && !phone.matches("\\d{10}")) {
            return false;
        }
        return true;
    }

    private void loadUserData(TextField fullNameField, TextField emailField, TextField phoneField) {
        String sql = "SELECT full_name, email, phone_number, profile_picture FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                fullNameField.setText(rs.getString("full_name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone_number") != null ? rs.getString("phone_number") : "");
                String picturePath = rs.getString("profile_picture");
                if (picturePath != null && !picturePath.isEmpty()) {
                    try {
                        profileImageView.setImage(new Image(new File(picturePath).toURI().toString()));
                    } catch (Exception e) {
                        System.out.println("Failed to load profile picture: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean updateProfile(String fullName, String email, String phone) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone_number = ? WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, phone.isEmpty() ? null : phone);
            stmt.setString(4, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateProfilePicture(String path) {
        String sql = "UPDATE users SET profile_picture = ? WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, path);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
