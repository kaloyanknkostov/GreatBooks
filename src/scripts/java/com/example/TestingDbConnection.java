package com.example;

import java.util.Scanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

public class TestingDbConnection {

    /*
    gradlew runScript -PscriptClass=com.example.OtherScript
    */
    public static void main(String[] args) {
        var app = new SpringApplication(ProjectConfig.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        var context = app.run(args);
        System.out.println("Started with Book Insertion");
        context.getBean(BookInsertion.class).runner();
        System.out.println("DONE with Book Insertion");
        context.getBean(SimpleTableInsertion.class).runner();
        context.close();
    }

    public static void main1(String[] args) {
        var app = new SpringApplication(ProjectConfig.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        var context = app.run(args);
        System.out.println("SCRIPTS ARE WORKING for now!");
        var scanner = new Scanner(System.in);
        System.out.println("Write username");
        var username = scanner.nextLine();
        System.out.println("Write email");
        var email = scanner.nextLine();
        System.out.println("Write password");
        var password = scanner.nextLine();
        scanner.close();
        context.getBean(DbConnector.class).tester(username, email, password);
        context.close();
    }
}
