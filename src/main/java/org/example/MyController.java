package org.example;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<FileSystemResource> display(@RequestParam("number") int number) {

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("./"+path+"TwitchDownloaderCLI", "chatdownload","-u", String.valueOf(number),"-o",path+number+".csv");
        System.out.println(processBuilder.command());
        System.out.println(processBuilder.directory());
        try {

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {

                System.out.println("Success!");
                System.out.println(output);
                converter(number);
                File file = new File(path+"templates/Success.html");
                FileSystemResource resource = new FileSystemResource(file);
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(resource);
            }else {
                System.out.println("Erreur lors de l'execution "+exitVal);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Number: " + number);

        File file = new File(path+"templates/Success.html");
        FileSystemResource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }
    void converter(int number){
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
            Database.saveObjects(chatLines,number);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
