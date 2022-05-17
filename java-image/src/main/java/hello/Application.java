package hello;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@SpringBootApplication
@RestController
@EnableScheduling
public class Application {
    @Autowired
    private Environment environment;

    @GetMapping("/env/{name}")
    public String env(@PathVariable("name") String name) {
        log.info("env:{},value is:{}", name, environment.getProperty(name));
        return String.format("env:%s,value is:%s",  name, environment.getProperty(name));
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    long index = 0L;
    @Scheduled(fixedRate = 3000)
    public void print() {
        log.info("hi!{},at:{}", index++, System.currentTimeMillis());
    }
}