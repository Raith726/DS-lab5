package pe.edu.utec.marketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class MarketingApplication {

    public static void main(String[] args) {

        SpringApplication.run(MarketingApplication.class, args);
    }
}
