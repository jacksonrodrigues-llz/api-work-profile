package api.work.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties
public class ApiWorkProfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiWorkProfileApplication.class, args);
    }

}
