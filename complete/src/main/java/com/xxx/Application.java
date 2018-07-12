package com.xxx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.qrtz.cache.QuartzApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
    	Class [] sources = new Class [] {Application.class, QuartzApplication.class};
        SpringApplication.run(sources, args);
    }
}
