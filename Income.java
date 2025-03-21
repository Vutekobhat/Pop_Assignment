package userInterface;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class Income {
    private VBox view;
    private String username;
    private Stage stage;
    private TableView<IncomeEntry> historyTable;

    public static class IncomeEntry {
        private final int id;
        private final double amount;
        private final String description;
        private final String source;
        private final LocalDate date;

        public IncomeEntry(int id, double amount, String description, String source, LocalDate date) {
            this.id = id;
            this.amount = amount;
            this.description = description;
            this.source = source;
            this.date = date;
        }

        public int getId() { return id; }
        public double getAmount() { return amount; }
        public String getDescription() { return description; }
        public String getSource() { return source; }
        public LocalDate getDate() { return date; }
    }

    public Income(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
        view = new VBox(10);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: #b0b3b8;");

        // Top bar with menu and welcome label
        Button menuButton = new Button("☰");
        menuButton.setStyle("-fx-background-color: #95989a; -fx-text-fill: #ffffff; -fx-font-size: 16px;");

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label welcomeLabel = new Label("Welcome, " + username + "!");
        welcomeLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 16px; -fx-font-weight: bold;");
        topBar.getChildren().addAll(menuButton, welcomeLabel);

        // Input fields
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");
        amountField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");

        TextField descField = new TextField();
        descField.setPromptText("Enter description");
        descField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");

        ComboBox<String> sourceCombo = new ComboBox<>();
        sourceCombo.getItems().addAll("Salary", "Freelance", "Investment", "Gift", "Other");
        sourceCombo.setValue("Other");
        sourceCombo.setStyle("-fx-background-color: #ffffff;");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-background-color: #ffffff;");

        Button addButton = new Button("Add Income");
        addButton.setStyle("-fx-background-color: #95989a; -fx-text-fill: #ffffff; -fx-font-weight: bold;");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #006400;");

        // History Table
        historyTable = new TableView<>();
        TableColumn<IncomeEntry, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<IncomeEntry, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<IncomeEntry, String> descColumn = new TableColumn<>("Description");
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<IncomeEntry, String> sourceColumn = new TableColumn<>("Source");
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));

        TableColumn<IncomeEntry, LocalDate> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<IncomeEntry, Void> deleteColumn = new TableColumn<>("Action");
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #8b0000; -fx-text-fill: #ffffff;");
                deleteButton.setOnAction(event -> {
                    IncomeEntry income = getTableView().getItems().get(getIndex());
                    deleteIncome(income.getId());
                    loadIncomes();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        historyTable.getColumns().addAll(idColumn, amountColumn, descColumn, sourceColumn, dateColumn, deleteColumn);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Back Button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #95989a; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.getChildren().add(backButton);

        // Add income action
        addButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String description = descField.getText();
                String source = sourceCombo.getValue();
                LocalDate date = datePicker.getValue();
                addIncome(amount, description, source, date);
                statusLabel.setText("Income added successfully!");
                statusLabel.setStyle("-fx-text-fill: #006400;");
                amountField.clear();
                descField.clear();
                loadIncomes();
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid amount!");
                statusLabel.setStyle("-fx-text-fill: #8b0000;");
            }
        });

        // Menu button action - Navigate to ProfilePage
        menuButton.setOnAction(e -> {
            ProfilePage profilePage = new ProfilePage(username);
            try {
                profilePage.start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Back button action - Navigate to Dashboard
        backButton.setOnAction(e -> {
            Dashboard dashboard = new Dashboard(stage, username);
            stage.getScene().setRoot(dashboard.getView());
        });

        view.getChildren().addAll(topBar, amountField, descField, sourceCombo, datePicker, addButton,
                                statusLabel, historyTable, bottomBar);
        loadIncomes();

        Scene scene = new Scene(view, 500, 450); // Consistent size
        stage.setScene(scene);
    }

    private void addIncome(double amount, String description, String source, LocalDate date) {
        String sql = "INSERT INTO incomes (user_id, amount, description, source, date) " +
                     "VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setDouble(2, amount);
            stmt.setString(3, description);
            stmt.setString(4, source);
            stmt.setDate(5, java.sql.Date.valueOf(date));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteIncome(int incomeId) {
        String sql = "DELETE FROM incomes WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, incomeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadIncomes() {
        historyTable.getItems().clear();
        String sql = "SELECT id, amount, description, source, date FROM incomes " +
                     "WHERE user_id = (SELECT id FROM users WHERE username = ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                historyTable.getItems().add(new IncomeEntry(
                    rs.getInt("id"),
                    rs.getDouble("amount"),
                    rs.getString("description"),
                    rs.getString("source"),
                    rs.getDate("date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public VBox getView() {
        return view;
    }
}