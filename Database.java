package userInterface;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

	public class Database {

		    private static final String URL = "jdbc:mysql://localhost:3306/expense_management";
		    private static final String USER = "root";
		    private static final String PASSWORD = "Myasalachor";

		    public static Connection getConnection() throws SQLException {
		        return DriverManager.getConnection(URL, USER, PASSWORD);
		    }
		}
