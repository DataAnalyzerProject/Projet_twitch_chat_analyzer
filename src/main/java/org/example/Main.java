package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication(scanBasePackages = "org.example")
public class Main {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /**
     * Generate random numbers publish with WebSocket protocol each 3 seconds.
     * @return a command line runner.
     */
    @Bean
    public CommandLineRunner websocketDemo() {
        Database database = new Database();
        database.test(messagingTemplate);
        return (args) -> {
            while (true) {
                try {
                    Thread.sleep(3*1000); // Each 3 sec.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * Get a random integer value in a min / max range.
     * @param min min range value
     * @param max max range value
     * @return A random integer value
     */
    private int randomWithRange(int min, int max){
        int range = Math.abs(max - min) + 1;
        return (int)(Math.random() * range) + (Math.min(min, max));
    }
}
