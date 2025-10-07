package dev.kuku.authsome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthSomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthSomeApplication.class, args);
    }
}
//TODO CRON job for cleaning up refresh token