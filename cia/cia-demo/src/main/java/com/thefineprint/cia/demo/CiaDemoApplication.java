package com.thefineprint.cia.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Track C demonstration application: runs the assessed algorithms server-side behind {@code /algo/*} and serves
 * a browser UI from {@code static/}. It holds no state between requests, has no database, makes no outbound
 * calls, and accepts synthetic input only.
 */
@SpringBootApplication
public class CiaDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CiaDemoApplication.class, args);
    }
}
