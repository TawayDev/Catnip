package dev.taway.catnip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CatnipApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatnipApplication.class, args);
        System.out.println(
                """
                          ___   __  ____  __ _  __  ____\s
                         / __) / _\\(_  _)(  ( \\(  )(  _ \\
                        ( (__ /    \\ )(  /    / )(  ) __/
                         \\___)\\_/\\_/(__) \\_)__)(__)(__) \s
                               Made by Taway (with love)"""
        );
    }
}
