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
//TODO possible to make things transactional BUT only for certain things? such as transactional but where userId is X so that other users can continue doing their thing
//NOTE: Project config. REfresh token rotation method : None, Extended, Rotate but do not extend
//NOTE : Hard limit for custom attributes and data that user can put or else they may put huge data