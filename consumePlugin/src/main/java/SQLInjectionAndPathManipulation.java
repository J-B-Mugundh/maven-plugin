import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.servlet.http.HttpServletRequest;

public class SQLInjectionAndPathManipulation {

    public void handleRequest(HttpServletRequest request) {
        try {
            String userId = request.getParameter("userId");
            String query = "SELECT * FROM users WHERE id = '" + userId + "'";
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb", "root", "password");
            Statement statement = connection.createStatement();
            statement.executeQuery(query);

            String filePath = request.getParameter("filePath");
            File file = new File(filePath);
            if (file.exists()) {
                System.out.println("File exists: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
