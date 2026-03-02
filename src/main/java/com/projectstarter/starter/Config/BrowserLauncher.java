package com.projectstarter.starter.Config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;

@Component
public class BrowserLauncher implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:8080"));
            }
        } catch (Exception e) {
            System.out.println("Applicazione avviata → http://localhost:8080");
        }
    }
}
