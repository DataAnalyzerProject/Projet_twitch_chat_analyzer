package org.example;

import java.sql.*;
import java.util.ArrayList;

public class Database {

    public static void saveObjects(ArrayList<ChatLine> chatLineArrayList, int StreamId) {
        try {
            String username = "root";
            String password = "my-secret-pw";
            String url = "jdbc:mysql://host.docker.internal:3306/test";
            Connection conn = DriverManager.getConnection(url, username, password);
            for (int i = 0; i < chatLineArrayList.size()-1; i++) {
                System.out.println("Avancement "+i+"/"+chatLineArrayList.size());
                String query = "INSERT INTO ChatData (time, user, message, idStream) VALUES ( ?, ?, ?,?)";
                PreparedStatement preparedStmt = conn.prepareStatement(query);
                preparedStmt.setString(1, chatLineArrayList.get(i).getHeure());
                preparedStmt.setString(2, chatLineArrayList.get(i).getUser());
                preparedStmt.setString(3, chatLineArrayList.get(i).getMessage());
                preparedStmt.setInt(4, StreamId);
                preparedStmt.execute();
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
