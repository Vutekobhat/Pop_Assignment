package userInterface;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Dashboard {
    private Stage stage;
    private String username; //username introduced
    private Label balanceLabel; //balance introduced
    private Label incomeLabel; //income introduced
    private Label expenseLabel; //expense introduced
    private Label warningLabel; // Added for expense limit warning

    public Dashboard(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
    }

    public void show() {
        VBox root = new VBox(20); //Vbox introduced to maintain size and color
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f0f0f0;");

        //Creating Menu bar and assigning items
        MenuBar menuBar = new MenuBar(); 
        Menu menu = new Menu("Menu");
        menu.setStyle("-fx-font-size: 16px;");
        MenuItem profileItem = new MenuItem("Profile");
        MenuItem transactionsItem = new MenuItem("Transactions");
        MenuItem settingsItem = new MenuItem("Settings");
        MenuItem logoutItem = new MenuItem("Logout");
        menu.getItems().addAll(profileItem, transactionsItem, settingsItem, logoutItem);
        menuBar.getMenus().add(menu);

        Label welcomeLabel = new Label("Welcome, " + username + "!");
        welcomeLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        //Hbox introduced for alignment 
        HBox summaryBox = new HBox(50);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setPadding(new Insets(20));
        summaryBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d3d3d3; -fx-border-radius: 5; -fx-background-radius: 5;");

        balanceLabel = new Label("Current Balance: $0.00");
        incomeLabel = new Label("Total Income: $0.00");
        expenseLabel = new Label("Total Expenses: $0.00");
        balanceLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        incomeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        expenseLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #F44336;");
        summaryBox.getChildren().addAll(balanceLabel, incomeLabel, expenseLabel);

        // Add warning label for expense limit
        warningLabel = new Label();
        warningLabel.setStyle("-fx-font-size: 16px;");

        //Creating bar chart and aligning them 
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Amount ($)");
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Income vs Expenses (Monthly)");
        barChart.setPrefHeight(600);
        barChart.setStyle("-fx-font-size: 16px;");

        updateSummary();
        updateChart(barChart);

        //Giving function to items on Menu bar
        profileItem.setOnAction(e -> {
            ProfilePage profilePage = new ProfilePage(stage, username);
            profilePage.show();
        });

        transactionsItem.setOnAction(e -> {
            Transactions transactions = new Transactions(stage, username);
            transactions.show();
        });

        settingsItem.setOnAction(e -> {
            Settings settings = new Settings(stage, username);
            settings.show();
        });

        logoutItem.setOnAction(e -> {
            Login login = new Login(stage);
            login.show();
        });

        root.getChildren().addAll(menuBar, welcomeLabel, summaryBox, warningLabel, barChart);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Dashboard - " + username);
        stage.setMaximized(true);
        stage.show();
    }

    private void updateSummary() {
        try (Connection conn = Database.getConnection()) {
            int userId = getUserId(conn);
            if (userId == -1) {
                System.out.println("User ID not found for username: " + username);
                return;
            }

            // Calculate total income
            String incomeSql = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'INCOME'";
            PreparedStatement incomeStmt = conn.prepareStatement(incomeSql);
            incomeStmt.setInt(1, userId);
            ResultSet incomeRs = incomeStmt.executeQuery();
            double totalIncome = incomeRs.next() ? incomeRs.getDouble(1) : 0;

            // Calculate total expenses
            String expenseSql = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'EXPENSE'";
            PreparedStatement expenseStmt = conn.prepareStatement(expenseSql);
            expenseStmt.setInt(1, userId);
            ResultSet expenseRs = expenseStmt.executeQuery();
            double totalExpense = expenseRs.next() ? expenseRs.getDouble(1) : 0;

            // Calculate current month's expenses
            LocalDate now = LocalDate.now();
            String monthYear = now.getYear() + "-" + String.format("%02d", now.getMonthValue());
            String monthlyExpenseSql = "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'EXPENSE' AND date LIKE ?";
            PreparedStatement monthlyExpenseStmt = conn.prepareStatement(monthlyExpenseSql);
            monthlyExpenseStmt.setInt(1, userId);
            monthlyExpenseStmt.setString(2, monthYear + "%");
            ResultSet monthlyExpenseRs = monthlyExpenseStmt.executeQuery();
            double monthlyExpense = monthlyExpenseRs.next() ? monthlyExpenseRs.getDouble(1) : 0;

            // Fetch the user's expense limit
            String limitSql = "SELECT expense_limit FROM users WHERE id = ?";
            PreparedStatement limitStmt = conn.prepareStatement(limitSql);
            limitStmt.setInt(1, userId);
            ResultSet limitRs = limitStmt.executeQuery();
            Double expenseLimit = null;
            if (limitRs.next()) {
                expenseLimit = limitRs.getDouble("expense_limit");
                if (limitRs.wasNull()) {
                    expenseLimit = null; // Handle NULL expense_limit
                }
            }

            // Update summary labels
            incomeLabel.setText(String.format("Total Income: $%.2f", totalIncome));
            expenseLabel.setText(String.format("Total Expenses: $%.2f", totalExpense));
            balanceLabel.setText(String.format("Current Balance: $%.2f", totalIncome - totalExpense));

            // Check if monthly expenses exceed the limit and display warning
            if (expenseLimit != null && monthlyExpense > expenseLimit) {
                warningLabel.setText(String.format("Warning: Monthly expenses ($%.2f) exceed your limit ($%.2f)!", monthlyExpense, expenseLimit));
                warningLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            } else {
                warningLabel.setText(""); // Clear warning if no violation
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateChart(BarChart<String, Number> barChart) {
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");

        try (Connection conn = Database.getConnection()) {
            int userId = getUserId(conn);
            if (userId == -1) {
                System.out.println("User ID not found for username: " + username);
                return;
            }

            String sql = "SELECT date, type, amount FROM transactions WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            double[] incomeByMonth = new double[12];
            double[] expenseByMonth = new double[12];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            while (rs.next()) {
                String dateStr = rs.getString("date");
                LocalDate date = LocalDate.parse(dateStr, formatter);
                int month = date.getMonthValue() - 1;
                double amount = rs.getDouble("amount");
                String type = rs.getString("type");

                if (type.equals("INCOME")) {
                    incomeByMonth[month] += amount;
                } else if (type.equals("EXPENSE")) {
                    expenseByMonth[month] += amount;
                }
            }

            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            for (int i = 0; i < 12; i++) {
                incomeSeries.getData().add(new XYChart.Data<>(months[i], incomeByMonth[i]));
                expenseSeries.getData().add(new XYChart.Data<>(months[i], expenseByMonth[i]));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        barChart.getData().clear();
        barChart.getData().addAll(incomeSeries, expenseSeries);
    }

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



