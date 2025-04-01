package userInterface;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Transactions {
    private Stage stage;
    private String username;
    private TableView<TransactionEntry> transactionTable;

    public static class TransactionEntry {
        private final int id;
        private final String type;
        private final double amount;
        private final String description;
        private final String source;
        private final String date;

        public TransactionEntry(int id, String type, double amount, String description, String source, String date)
        {
            this.id = id;
            this.type = type;
            this.amount = amount;
            this.description = description;
            this.source = source;
            this.date = date;
        }

        //Elements introduced
        public int getId() { return id; }
        public String getType() { return type; }
        public double getAmount() { return amount; }
        public String getDescription() { return description; }
        public String getSource() { return source; }
        public String getDate() { return date; }
    }

    public Transactions(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }
    
    //Window alignment
    public void show() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f0f0f0;");

        //Label and Title
        Label titleLabel = new Label("Transactions");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("INCOME", "EXPENSE");
        typeCombo.setValue("INCOME");
        typeCombo.setStyle("-fx-font-size: 16px;");
        typeCombo.setPrefWidth(200);

        //TextField
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setStyle("-fx-font-size: 16px;");
        amountField.setPrefWidth(200);

        TextField descField = new TextField();
        descField.setPromptText("Description (Optional)");
        descField.setStyle("-fx-font-size: 16px;");
        descField.setPrefWidth(200);

        TextField sourceField = new TextField();
        sourceField.setPromptText("Source/Category (Optional)");
        sourceField.setStyle("-fx-font-size: 16px;");
        sourceField.setPrefWidth(200);

        //Date picker
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-font-size: 16px;");
        datePicker.setPrefWidth(200);

        //Button
        Button addButton = new Button("Add Transaction");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;");
        addButton.setPrefWidth(200);
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 16px;");

        //Transaction Table
        transactionTable = new TableView<>();
        TableColumn<TransactionEntry, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(100); // Adjusted column width
        TableColumn<TransactionEntry, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(150);
        TableColumn<TransactionEntry, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(150);
        TableColumn<TransactionEntry, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);
        TableColumn<TransactionEntry, String> sourceCol = new TableColumn<>("Source");
        sourceCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceCol.setPrefWidth(200);
        TableColumn<TransactionEntry, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(150);
        transactionTable.getColumns().addAll(idCol, typeCol, amountCol, descCol, sourceCol, dateCol);
        transactionTable.setPrefHeight(500); // Increased height for larger screen

        //Scroll bar
        ScrollPane scrollPane = new ScrollPane(transactionTable);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(520);

        //Button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px;");
        backButton.setPrefWidth(200);

        //Giving action to button
        addButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) {
                    statusLabel.setText("Amount must be positive!");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    return;
                }
                String type = typeCombo.getValue();
                String description = descField.getText().trim();
                String source = sourceField.getText().trim();
                LocalDate date = datePicker.getValue();
                if (date == null) {
                    statusLabel.setText("Please select a date!");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    return;
                }
                addTransaction(type, amount, description, source, date);
                statusLabel.setText("Transaction added!");
                statusLabel.setStyle("-fx-text-fill: green;");
                amountField.clear();
                descField.clear();
                sourceField.clear();
                datePicker.setValue(LocalDate.now());
                loadTransactions();
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid amount!");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        backButton.setOnAction(e -> {
            Dashboard dashboard = new Dashboard(stage, username);
            dashboard.show();
        });

        root.getChildren().addAll(titleLabel, typeCombo, amountField, descField, sourceField, datePicker,
                addButton, statusLabel, scrollPane, backButton);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Transactions - " + username);
        stage.setMaximized(true); // Maximize the window
        stage.show();

        loadTransactions();
    }
    
    
    //Transaction
    private void addTransaction(String type, double amount, String description, String source, LocalDate date) {
        String sql = "INSERT INTO transactions (user_id, type, amount, description, source, date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, getUserId(conn));
            stmt.setString(2, type);
            stmt.setDouble(3, amount);
            stmt.setString(4, description.isEmpty() ? null : description);
            stmt.setString(5, source.isEmpty() ? null : source);
            stmt.setString(6, date.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTransactions() {
        transactionTable.getItems().clear();
        String sql = "SELECT id, type, amount, description, source, date FROM transactions WHERE user_id = ? ORDER BY date DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, getUserId(conn));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactionTable.getItems().add(new TransactionEntry(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("description") != null ? rs.getString("description") : "",
                        rs.getString("source") != null ? rs.getString("source") : "",
                        rs.getString("date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //User
    private int getUserId(Connection conn) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }
}
