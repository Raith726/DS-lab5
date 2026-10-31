package pe.edu.utec.champions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class ChampionsApplication {

    public static void main(String[] args) {

        SpringApplication.run(ChampionsApplication.class, args);
    }
}
