package userInterface;                                                                                                                                      
                                                                                                                                                            
import java.sql.Connection;                                                                                                                                 
import java.sql.DriverManager;                                                                                                                              
import java.sql.SQLException;                                                                                                                               
import java.sql.Statement;                                                                                                                                  
                                                                                                                                                            
public class Database {                                                                                                                                     
    private static final String URL = "jdbc:mysql://localhost:3306/expense_management?useSSL=false";                                                        
    private static final String USER = "root"; // Replace with your MySQL username                                                                          
    private static final String PASSWORD = "Myasalachor"; // Replace with your MySQL password                                                               
                                                                                                                                                            
    public static Connection getConnection() throws SQLException {                                                                                          
        return DriverManager.getConnection(URL, USER, PASSWORD);                                                                                            
    }                                                                                                                                                       
       //Database initialized                                                                                                                                  
    public static void initializeDatabase() {                                                                                                               
        try (Connection conn = getConnection();                                                                                                             
             Statement stmt = conn.createStatement()) { 
        	//UserTable initialized
            String usersTable = "CREATE TABLE IF NOT EXISTS users (" +                                                                                      
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +                                                                                                 
                    "full_name VARCHAR(255) NOT NULL, " +                                                                                                   
                    "username VARCHAR(50) NOT NULL UNIQUE, " +                                                                                              
                    "phone_number VARCHAR(15), " +                                                                                                          
                    "email VARCHAR(100) NOT NULL UNIQUE, " +                                                                                                
                    "password VARCHAR(255) NOT NULL, " +                                                                                                    
                    "profile_picture VARCHAR(255), " +                                                                                                      
                    "expense_limit DOUBLE DEFAULT NULL)"; // Added expense_limit column                                                                     
            stmt.execute(usersTable);                                                                                                                       
                               
          //TransactionsTable initialized
            String transactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +                                                                        
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +                                                                                                 
                    "user_id INT NOT NULL, " +                                                                                                              
                    "type VARCHAR(10) NOT NULL, " +                                                                                                         
                    "amount DOUBLE NOT NULL, " +                                                                                                            
                    "description TEXT, " +                                                                                                                  
                    "source VARCHAR(100), " +                                                                                                               
                    "date VARCHAR(10) NOT NULL, " +                                                                                                         
                    "FOREIGN KEY(user_id) REFERENCES users(id))";                                                                                           
            stmt.execute(transactionsTable);                                                                                                                
        } catch (SQLException e) {                                                                                                                          
            e.printStackTrace();                                                                                                                            
        }                                                                                                                                                   
    }                                                                                                                                                       
}                                                                                                                                                           
                                                                                                                                                            
                                                                                                                                                            
                                                                                                                                                        
