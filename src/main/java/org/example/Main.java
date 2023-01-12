package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

// Classe représentant un message de chat
@AllArgsConstructor
@ToString
@Getter
@Setter
class ChatMessage {
    private String temps;
    private String utilisateur;
    private String message;
}

public class Main {
    public static void main(String[] args) {

        // Créez une liste d'objets ChatMessage
        List<ChatMessage> messages = new ArrayList<>();
        InputStream stream = Main.class.getClassLoader().getResourceAsStream("chat.txt");
        try {
            // Ouvrez le fichier de texte en lecture
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            // Lisez chaque ligne du fichier
            String message = reader.readLine();
            while (message != null) {

                System.out.println(message);

                int startIndex = message.indexOf("[");
                int endIndex = message.indexOf("]");

                String heure = message.substring(startIndex + 1, endIndex);

                startIndex = message.indexOf(':',  message.indexOf(':',message.indexOf(':')+1)+1);

                String pseudo = message.substring(endIndex+2, startIndex);

                message = message.substring(startIndex + 2);

                // Créez un objet ChatMessage à partir des données extraites
                ChatMessage chat = new ChatMessage(heure,pseudo,message);
                System.out.println(chat +"\n");

                // Ajoutez l'objet à la liste
                messages.add(chat);

                // Lisez la prochaine ligne
                message = reader.readLine();
            }

            // Fermez le fichier
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
