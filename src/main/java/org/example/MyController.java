package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/home")
public class MyController {

    String path ="src/main/resources/";
    Database database = new Database();
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @RequestMapping("/")
    public ResponseEntity<Resource> home(){
        File file = new File(path+"templates/Home.html");
        FileSystemResource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @RequestMapping("/telechargement")
    public ResponseEntity<Resource> getTextFile() {
        File file = new File(path+"templates/WebPage.html");
        FileSystemResource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @RequestMapping("/index")
    String index(){
        return "Hello from index";
    }

    @PostMapping("/display")
    public void display(@RequestParam("number") int number) {
        if(database.exist(number)){
            messagingTemplate.convertAndSend("/topic/progress",-1);
        }else {
            ProcessBuilder processBuilder = new ProcessBuilder();
            //    processBuilder.command("./"+path+"TwitchDownloaderCLI", "chatdownload","-u", String.valueOf(number),"-o",path+number+".csv");
            processBuilder.command(path + "TwitchDownloaderCLI.exe", "chatdownload", "-u", String.valueOf(number), "-o", path + number + ".csv");
            System.out.println(processBuilder.command());
            System.out.println(processBuilder.directory());
            try {

                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()
                        ));

                String line;
                String pattern = "(\\d+)%";

                Pattern r = Pattern.compile(pattern);

                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    Matcher m = r.matcher(line);
                    if (m.find()) {
                        int percent = Integer.parseInt(m.group(1));
                        System.out.println("Percentage: " + percent);
                        messagingTemplate.convertAndSend("/topic/progress", percent);

                    }
                }
                int exitVal = process.waitFor();
                if (exitVal == 0) {
                    messagingTemplate.convertAndSend("/topic/progress", 99);
                    System.out.println("Success!");
                    converter(number);
                } else {
                    System.out.println("Erreur lors de l'execution " + exitVal);
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Number: " + number);
        }
    }
    void converter(int number){
        Database database = new Database();
        ArrayList<ChatLine> chatLines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/"+number+".csv"));
            String line;
            while ((line = reader.readLine()) != null) {

                Pattern pattern = Pattern.compile("\\[(.*?)\\] (.*?): (.*)");
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    String outputFormatString = "yyyy-MM-dd HH:mm:ss";
                    SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss");
                    SimpleDateFormat outputFormat = new SimpleDateFormat(outputFormatString);

                    Date date = inputFormat.parse(matcher.group(1));
                    String outputDate = outputFormat.format(date);
                    String pseudo = matcher.group(2);
                    String message = matcher.group(3);
                    ChatLine chatLine = new ChatLine(outputDate, pseudo, message);
                    chatLines.add(chatLine);
                }
            }
            reader.close();
            System.out.println(chatLines.get(1));
            database.saveObjects(chatLines,number,messagingTemplate);
            System.out.println("Stockage terminé");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
