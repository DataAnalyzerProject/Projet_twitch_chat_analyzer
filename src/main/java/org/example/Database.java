package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

@Component
public class Database {


    public void saveObjects(ArrayList<ChatLine> chatLineArrayList, int StreamId, SimpMessageSendingOperations messagingTemplate) {
        try {
            String username = "root";
            String password = "my-secret-pw";
            String url = "jdbc:mysql://host.docker.internal:3306/test";
            Connection conn = DriverManager.getConnection(url, username, password);

            //Désactiver l'auto-commit pour regrouper les insertions en une seule transaction
            conn.setAutoCommit(false);

            //Créer une requête préparée pour les insertions
            String query = "INSERT INTO ChatData (time, user, message, idStream) VALUES ( ?, ?, ?,?)";
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            for (float i = 0; i < chatLineArrayList.size(); i++) {
                if(i%100==0)System.out.println("Avancement "+i+"/"+chatLineArrayList.size());
                if(i>chatLineArrayList.size()-10)
                    System.out.println("bientot la fin");
                //Ajouter les valeurs à la requête préparée
                preparedStmt.setString(1, chatLineArrayList.get((int) i).getHeure());
                preparedStmt.setString(2, chatLineArrayList.get((int) i).getUser());
                preparedStmt.setString(3, chatLineArrayList.get((int) i).getMessage());
                preparedStmt.setInt(4, StreamId);

                //Ajouter la requête préparée à la liste des commandes à exécuter
                preparedStmt.addBatch();

            }

            //Exécuter toutes les commandes en une seule instruction
            System.out.println(Arrays.toString(preparedStmt.executeBatch()));

            //Valider la transaction
            conn.commit();

            //Fermer la connexion
            conn.close();
            messagingTemplate.convertAndSend("/topic/progress",100 );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void test(SimpMessageSendingOperations messagingTemplate){
        messagingTemplate.convertAndSend("/topic/progress",50 );
    }
    public boolean exist(int number){

        try {
            String username = "root";
            String password = "my-secret-pw";
            String url = "jdbc:mysql://host.docker.internal:3306/test";
            ResultSet rs;
            Connection conn = DriverManager.getConnection(url, username, password);
            String query = "SELECT CASE WHEN (SELECT COUNT(*) FROM ChatData WHERE IdStream = ?) = 0 THEN 0 ELSE 1 END AS result";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, String.valueOf(number));
            rs = preparedStmt.executeQuery();
            rs.next();
            return !(rs.getInt(1) == 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
