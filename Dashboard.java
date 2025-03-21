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

public class Dashboard {
    private VBox view;
    private String username;
    private Stage stage;
    private TableView<Expense> expenseTable;
    private TableView<Income> incomeTable;
    private Label todayExpenseLabel;
    private Label monthExpenseLabel;
    private Label allTimeExpenseLabel;
    private Label todayIncomeLabel;
    private Label monthIncomeLabel;
    private Label allTimeIncomeLabel;

    // Expense class
    public static class Expense {
        private final int id;
        private final double amount;
        private final String description;
        private final String category;
        private final LocalDate date;

        public Expense(int id, double amount, String description, String category, LocalDate date) {
            this.id = id;
            this.amount = amount;
            this.description = description;
            this.category = category;
            this.date = date;
        }

        public int getId() { return id; }
        public double getAmount() { return amount; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public LocalDate getDate() { return date; }
    }

    // Income class
    public static class Income {
        private final int id;
        private final double amount;
        private final String description;
        private final String source;
        private final LocalDate date;

        public Income(int id, double amount, String description, String source, LocalDate date) {
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

    public Dashboard(Stage stage, String username) {
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

        // Expense Input Fields
        Label expenseLabel = new Label("Add Expense");
        expenseLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextField expenseAmountField = new TextField();
        expenseAmountField.setPromptText("Enter expense amount");
        expenseAmountField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");

        TextField expenseDescField = new TextField();
        expenseDescField.setPromptText("Enter expense description");
        expenseDescField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");

        ComboBox<String> expenseCategoryCombo = new ComboBox<>();
        expenseCategoryCombo.getItems().addAll("Food", "Transport", "Entertainment", "Bills", "Shopping", "Other");
        expenseCategoryCombo.setValue("Other");
        expenseCategoryCombo.setStyle("-fx-background-color: #ffffff;");

        DatePicker expenseDatePicker = new DatePicker(LocalDate.now());
        expenseDatePicker.setStyle("-fx-background-color: #ffffff;");

        Button addExpenseButton = new Button("Add Expense");
        addExpenseButton.setStyle("-fx-background-color: #95989a; -fx-text-fill: #ffffff; -fx-font-weight: bold;");

        // Income Input Fields
        Label incomeLabel = new Label("Add Income");
        incomeLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px; -fx-font-weight: bold;");

        TextField incomeAmountField = new TextField();
        incomeAmountField.setPromptText("Enter income amount");
        incomeAmountField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");

        TextField incomeDescField = new TextField();
        incomeDescField.setPromptText("Enter income description");
        incomeDescField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333;");

        ComboBox<String> incomeSourceCombo = new ComboBox<>();
        incomeSourceCombo.getItems().addAll("Salary", "Freelance", "Investment", "Gift", "Other");
        incomeSourceCombo.setValue("Other");
        incomeSourceCombo.setStyle("-fx-background-color: #ffffff;");

        DatePicker incomeDatePicker = new DatePicker(LocalDate.now());
        incomeDatePicker.setStyle("-fx-background-color: #ffffff;");

        Button addIncomeButton = new Button("Add Income");
        addIncomeButton.setStyle("-fx-background-color: #95989a; -fx-text-fill: #ffffff; -fx-font-weight: bold;");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #006400;");

        // Expense History Table
        expenseTable = new TableView<>();
        TableColumn<Expense, Integer> expenseIdColumn = new TableColumn<>("ID");
        expenseIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Expense, Double> expenseAmountColumn = new TableColumn<>("Amount");
        expenseAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<Expense, String> expenseDescColumn = new TableColumn<>("Description");
        expenseDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Expense, String> expenseCategoryColumn = new TableColumn<>("Category");
        expenseCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<Expense, LocalDate> expenseDateColumn = new TableColumn<>("Date");
        expenseDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Expense, Void> expenseDeleteColumn = new TableColumn<>("Action");
        expenseDeleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #8b0000; -fx-text-fill: #ffffff;");
                deleteButton.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    deleteExpense(expense.getId());
                    loadExpenses();
                    updateSummary();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
        expenseTable.getColumns().addAll(expenseIdColumn, expenseAmountColumn, expenseDescColumn, 
                                        expenseCategoryColumn, expenseDateColumn, expenseDeleteColumn);
        expenseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        expenseTable.setPrefHeight(150); // Limit height to fit both tables

        // Income History Table
        incomeTable = new TableView<>();
        TableColumn<Income, Integer> incomeIdColumn = new TableColumn<>("ID");
        incomeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Income, Double> incomeAmountColumn = new TableColumn<>("Amount");
        incomeAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<Income, String> incomeDescColumn = new TableColumn<>("Description");
        incomeDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Income, String> incomeSourceColumn = new TableColumn<>("Source");
        incomeSourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        TableColumn<Income, LocalDate> incomeDateColumn = new TableColumn<>("Date");
        incomeDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Income, Void> incomeDeleteColumn = new TableColumn<>("Action");
        incomeDeleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #8b0000; -fx-text-fill: #ffffff;");
                deleteButton.setOnAction(event -> {
                    Income income = getTableView().getItems().get(getIndex());
                    deleteIncome(income.getId());
                    loadIncomes();
                    updateSummary();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
        incomeTable.getColumns().addAll(incomeIdColumn, incomeAmountColumn, incomeDescColumn, 
                                       incomeSourceColumn, incomeDateColumn, incomeDeleteColumn);
        incomeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        incomeTable.setPrefHeight(150); // Limit height to fit both tables

        // Summary Box (Expenses + Income)
        VBox summaryBox = new VBox(5);
        summaryBox.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #95989a;");
        todayExpenseLabel = new Label("Today's Expenditure: $0.00");
        monthExpenseLabel = new Label("This Month's Expenditure: $0.00");
        allTimeExpenseLabel = new Label("All Time Expenditure: $0.00");
        todayIncomeLabel = new Label("Today's Income: $0.00");
        monthIncomeLabel = new Label("This Month's Income: $0.00");
        allTimeIncomeLabel = new Label("All Time Income: $0.00");
        todayExpenseLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px;");
        monthExpenseLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px;");
        allTimeExpenseLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px;");
        todayIncomeLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px;");
        monthIncomeLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px;");
        allTimeIncomeLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px;");
        summaryBox.getChildren().addAll(todayExpenseLabel, monthExpenseLabel, allTimeExpenseLabel,
                                        todayIncomeLabel, monthIncomeLabel, allTimeIncomeLabel);

        // Back Button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #95989a; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.getChildren().add(backButton);

        // Add Expense Action
        addExpenseButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(expenseAmountField.getText());
                String description = expenseDescField.getText();
                String category = expenseCategoryCombo.getValue();
                LocalDate date = expenseDatePicker.getValue();
                addExpense(amount, description, category, date);
                statusLabel.setText("Expense added successfully!");
                statusLabel.setStyle("-fx-text-fill: #006400;");
                expenseAmountField.clear();
                expenseDescField.clear();
                loadExpenses();
                updateSummary();
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid expense amount!");
                statusLabel.setStyle("-fx-text-fill: #8b0000;");
            }
        });

        // Add Income Action
        addIncomeButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(incomeAmountField.getText());
                String description = incomeDescField.getText();
                String source = incomeSourceCombo.getValue();
                LocalDate date = incomeDatePicker.getValue();
                addIncome(amount, description, source, date);
                statusLabel.setText("Income added successfully!");
                statusLabel.setStyle("-fx-text-fill: #006400;");
                incomeAmountField.clear();
                incomeDescField.clear();
                loadIncomes();
                updateSummary();
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid income amount!");
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

        // Back button action - Navigate to Login
        backButton.setOnAction(e -> {
            Login login = new Login();
            try {
                login.start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        view.getChildren().addAll(topBar, 
                                 expenseLabel, expenseAmountField, expenseDescField, expenseCategoryCombo, expenseDatePicker, addExpenseButton,
                                 incomeLabel, incomeAmountField, incomeDescField, incomeSourceCombo, incomeDatePicker, addIncomeButton,
                                 statusLabel, expenseTable, incomeTable, summaryBox, bottomBar);
        loadExpenses();
        loadIncomes();
        updateSummary();

        Scene scene = new Scene(view, 500, 450);
        stage.setScene(scene);
    }

    private void addExpense(double amount, String description, String category, LocalDate date) {
        String sql = "INSERT INTO expenses (user_id, amount, description, category, date) " +
                     "VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setDouble(2, amount);
            stmt.setString(3, description);
            stmt.setString(4, category);
            stmt.setDate(5, java.sql.Date.valueOf(date));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteExpense(int expenseId) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, expenseId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadExpenses() {
        expenseTable.getItems().clear();
        String sql = "SELECT id, amount, description, category, date FROM expenses " +
                     "WHERE user_id = (SELECT id FROM users WHERE username = ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                expenseTable.getItems().add(new Expense(
                    rs.getInt("id"),
                    rs.getDouble("amount"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getDate("date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        incomeTable.getItems().clear();
        String sql = "SELECT id, amount, description, source, date FROM incomes " +
                     "WHERE user_id = (SELECT id FROM users WHERE username = ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                incomeTable.getItems().add(new Income(
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

    private void updateSummary() {
        try (Connection conn = Database.getConnection()) {
            String userIdSql = "SELECT id FROM users WHERE username = ?";
            PreparedStatement userStmt = conn.prepareStatement(userIdSql);
            userStmt.setString(1, username);
            ResultSet userRs = userStmt.executeQuery();
            if (!userRs.next()) return;
            int userId = userRs.getInt("id");

            // Expense Summary
            String todayExpenseSql = "SELECT SUM(amount) FROM expenses WHERE user_id = ? AND date = ?";
            PreparedStatement todayExpenseStmt = conn.prepareStatement(todayExpenseSql);
            todayExpenseStmt.setInt(1, userId);
            todayExpenseStmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            ResultSet todayExpenseRs = todayExpenseStmt.executeQuery();
            double todayExpenseTotal = todayExpenseRs.next() ? todayExpenseRs.getDouble(1) : 0;
            todayExpenseLabel.setText(String.format("Today's Expenditure: $%.2f", todayExpenseTotal));

            String monthExpenseSql = "SELECT SUM(amount) FROM expenses WHERE user_id = ? AND " +
                                   "date >= ? AND date <= ?";
            PreparedStatement monthExpenseStmt = conn.prepareStatement(monthExpenseSql);
            monthExpenseStmt.setInt(1, userId);
            LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDate lastOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            monthExpenseStmt.setDate(2, java.sql.Date.valueOf(firstOfMonth));
            monthExpenseStmt.setDate(3, java.sql.Date.valueOf(lastOfMonth));
            ResultSet monthExpenseRs = monthExpenseStmt.executeQuery();
            double monthExpenseTotal = monthExpenseRs.next() ? monthExpenseRs.getDouble(1) : 0;
            monthExpenseLabel.setText(String.format("This Month's Expenditure: $%.2f", monthExpenseTotal));

            String allTimeExpenseSql = "SELECT SUM(amount) FROM expenses WHERE user_id = ?";
            PreparedStatement allTimeExpenseStmt = conn.prepareStatement(allTimeExpenseSql);
            allTimeExpenseStmt.setInt(1, userId);
            ResultSet allTimeExpenseRs = allTimeExpenseStmt.executeQuery();
            double allTimeExpenseTotal = allTimeExpenseRs.next() ? allTimeExpenseRs.getDouble(1) : 0;
            allTimeExpenseLabel.setText(String.format("All Time Expenditure: $%.2f", allTimeExpenseTotal));

            // Income Summary
            String todayIncomeSql = "SELECT SUM(amount) FROM incomes WHERE user_id = ? AND date = ?";
            PreparedStatement todayIncomeStmt = conn.prepareStatement(todayIncomeSql);
            todayIncomeStmt.setInt(1, userId);
            todayIncomeStmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            ResultSet todayIncomeRs = todayIncomeStmt.executeQuery();
            double todayIncomeTotal = todayIncomeRs.next() ? todayIncomeRs.getDouble(1) : 0;
            todayIncomeLabel.setText(String.format("Today's Income: $%.2f", todayIncomeTotal));

            String monthIncomeSql = "SELECT SUM(amount) FROM incomes WHERE user_id = ? AND " +
                                   "date >= ? AND date <= ?";
            PreparedStatement monthIncomeStmt = conn.prepareStatement(monthIncomeSql);
            monthIncomeStmt.setInt(1, userId);
            monthIncomeStmt.setDate(2, java.sql.Date.valueOf(firstOfMonth));
            monthIncomeStmt.setDate(3, java.sql.Date.valueOf(lastOfMonth));
            ResultSet monthIncomeRs = monthIncomeStmt.executeQuery();
            double monthIncomeTotal = monthIncomeRs.next() ? monthIncomeRs.getDouble(1) : 0;
            monthIncomeLabel.setText(String.format("This Month's Income: $%.2f", monthIncomeTotal));

            String allTimeIncomeSql = "SELECT SUM(amount) FROM incomes WHERE user_id = ?";
            PreparedStatement allTimeIncomeStmt = conn.prepareStatement(allTimeIncomeSql);
            allTimeIncomeStmt.setInt(1, userId);
            ResultSet allTimeIncomeRs = allTimeIncomeStmt.executeQuery();
            double allTimeIncomeTotal = allTimeIncomeRs.next() ? allTimeIncomeRs.getDouble(1) : 0;
            allTimeIncomeLabel.setText(String.format("All Time Income: $%.2f", allTimeIncomeTotal));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public VBox getView() {
        return view;
    }
}