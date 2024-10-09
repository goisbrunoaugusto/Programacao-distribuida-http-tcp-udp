package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabasePersistance {
    final Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" +
                    System.getenv("DBNAME"),System.getenv("DBUSERNAME"), System.getenv("DBPASSWORD"));

    public DatabasePersistance() throws SQLException {
    }

    public List<String> getMessages() {
        List<String> messages = new ArrayList<>();
        String sql = "SELECT * FROM mensagens";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(rs.getString("conteudo"));
            }
            return messages;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar os mensagens", e);
        }
    }

    public void postMessage(String message) {
        String sql = "INSERT INTO mensagens (conteudo) VALUES (?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, message);
            pstmt.executeUpdate();
        }catch (SQLException e) {
            throw new RuntimeException("Erro ao postar os mensagens", e);
        }
    }
}
