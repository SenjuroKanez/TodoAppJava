package com.todoapp;

/** Isolated browser-test server only. H2 is never shipped in the production JAR. */
public class TestServer {
    public static void main(String[] args){
        System.setProperty("spring.datasource.url","jdbc:h2:mem:browser;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        System.setProperty("spring.datasource.username","sa");System.setProperty("spring.datasource.password","");
        System.setProperty("spring.datasource.driver-class-name","org.h2.Driver");
        System.setProperty("server.servlet.session.cookie.secure","false");
        System.setProperty("vaadin.productionMode","true");Application.main(args);
    }
}
