package com.fatture.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController implements ErrorController {
    
    /**
     * Serve index.html per la root
     */
    @RequestMapping("/")
    public String index() {
        return "forward:/index.html";
    }
    
    /**
     * Gestisce tutte le route non trovate (404) e le reindirizza a index.html
     * Questo permette al frontend Angular di gestire il routing SPA
     */
    @RequestMapping("/error")
    public String handleError() {
        return "forward:/index.html";
    }
    
    /**
     * Serve index.html per le route del frontend (SPA routing)
     */
    @RequestMapping(value = {
        "/fatture",
        "/fatture/**",
        "/clienti",
        "/clienti/**",
        "/statistiche",
        "/statistiche/**"
    })
    public String spaRoutes() {
        return "forward:/index.html";
    }
}
