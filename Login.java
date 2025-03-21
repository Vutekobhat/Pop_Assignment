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

public class Login extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setStyle("-fx-background-color: #b0b3b8;");

        Label lblTitle = new Label("Login");
        lblTitle.setFont(new Font("Arial", 18));
        lblTitle.setStyle("-fx-text-fill: #333333;");
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().add(lblTitle);
        grid.add(titleBox, 0, 0, 2, 1);

        Label lblEmail = new Label("Email:");
        lblEmail.setFont(new Font("Arial", 14));
        lblEmail.setStyle("-fx-text-fill: #333333;");
        grid.add(lblEmail, 0, 1);

        TextField txtEmail = new TextField();
        txtEmail.setPrefWidth(180);
        txtEmail.setPromptText("Enter email");
        txtEmail.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");
        grid.add(txtEmail, 1, 1);

        Label lblPassword = new Label("Password:");
        lblPassword.setFont(new Font("Arial", 14));
        lblPassword.setStyle("-fx-text-fill: #333333;");
        grid.add(lblPassword, 0, 2);

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPrefWidth(180);
        txtPassword.setPromptText("Enter password");
        txtPassword.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");
        grid.add(txtPassword, 1, 2);

        Button btnLogin = new Button("Login");
        btnLogin.setStyle("-fx-background-color: #95989a; -fx-text-fill: #ffffff; -fx-font-size: 14px;");

        Button btnRegister = new Button("Register");
        btnRegister.setStyle("-fx-background-color: #95989a; -fx-text-fill: #ffffff; -fx-font-size: 14px;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(btnLogin, btnRegister);
        grid.add(buttonBox, 1, 3);

        Label lblStatus = new Label("");
        grid.add(lblStatus, 1, 4);

        btnLogin.setOnAction(e -> {
            String email = txtEmail.getText();
            String password = txtPassword.getText();
            if (validateInput(email, password)) {
                String username = authenticate(email, password);
                if (username != null) {
                    lblStatus.setText("Login successful!");
                    lblStatus.setStyle("-fx-text-fill: #006400;");
                    Dashboard dashboard = new Dashboard(primaryStage, username);
                    primaryStage.getScene().setRoot(dashboard.getView());
                } else {
                    lblStatus.setText("Invalid email or password");
                    lblStatus.setStyle("-fx-text-fill: #8b0000;");
                }
            } else {
                lblStatus.setText("Please enter valid email and password");
                lblStatus.setStyle("-fx-text-fill: #8b0000;");
            }
        });

        btnRegister.setOnAction(e -> {
            SignUp signUp = new SignUp();
            try {
                signUp.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Scene scene = new Scene(grid, 500, 450); // Consistent size
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            return false;
        }
        return true;
    }

    private String authenticate(String email, String password) {
        String sql = "SELECT username FROM users WHERE email = ? AND password = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password); // In production, use hashed passwords
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}