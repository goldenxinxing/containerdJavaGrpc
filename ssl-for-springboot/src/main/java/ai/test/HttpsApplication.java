package ai.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class HttpsApplication {
    public static void main(String[] args) {
        SpringApplication.run(HttpsApplication.class, args);
    }

    @RestController
    class Api{
        @GetMapping("demo")
        public String demo() {
            return "success";
        }
    }
}
