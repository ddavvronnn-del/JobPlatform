package uz.imaan.jobplatform.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;


public class Swagger {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Card API")
                        .version("1.0")
                        .description("Simple Card Management API"));
    }
}
