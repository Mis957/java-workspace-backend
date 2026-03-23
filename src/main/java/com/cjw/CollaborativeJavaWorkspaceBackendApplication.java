package com.cjw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.collab.workspace.backend")
public class CollaborativeJavaWorkspaceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollaborativeJavaWorkspaceBackendApplication.class, args);
    }
}
