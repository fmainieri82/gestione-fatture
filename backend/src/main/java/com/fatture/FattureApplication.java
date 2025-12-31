package com.fatture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

@SpringBootApplication
public class FattureApplication {

    public static void main(String[] args) {
        // Crea cartelle necessarie se non esistono
        createDirectories();
        
        // Avvia Spring Boot
        SpringApplication app = new SpringApplication(FattureApplication.class);
        
        // Aggiungi listener per aprire il browser quando l'applicazione è pronta
        app.addListeners((ApplicationListener<ApplicationReadyEvent>) event -> {
            openBrowser();
        });
        
        app.run(args);
    }
    
    private static void createDirectories() {
        new File("data").mkdirs();
        new File("templates").mkdirs();
        new File("fatture").mkdirs();
        new File("backups").mkdirs();
    }
    
    private static void openBrowser() {
        try {
            Thread.sleep(2000); // Attendi che il server sia pronto
            
            String url = "http://localhost:8080";
            boolean opened = false;
            
            // Prova con Desktop API
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(new URI(url));
                        opened = true;
                    }
                } catch (Exception e) {
                    System.out.println("Desktop API non disponibile, uso comando sistema...");
                }
            }
            
            // Fallback: usa comando del sistema operativo
            if (!opened) {
                String os = System.getProperty("os.name").toLowerCase();
                Runtime runtime = Runtime.getRuntime();
                
                if (os.contains("win")) {
                    // Windows
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.contains("mac")) {
                    // macOS
                    runtime.exec("open " + url);
                } else {
                    // Linux e altri
                    runtime.exec("xdg-open " + url);
                }
                opened = true;
            }
            
            if (opened) {
                System.out.println("Browser aperto su: " + url);
            } else {
                System.out.println("Impossibile aprire il browser automaticamente.");
                System.out.println("Apri manualmente: " + url);
            }
        } catch (Exception e) {
            System.out.println("Errore nell'apertura del browser: " + e.getMessage());
            System.out.println("Apri manualmente: http://localhost:8080");
        }
    }
    
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200", "http://localhost:8080")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
