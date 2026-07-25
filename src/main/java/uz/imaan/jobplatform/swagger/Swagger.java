package uz.imaan.jobplatform.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Admin Management API",
                version = "1.0",
                description = "Endpoints for managing admin users"
        )
)
public class Swagger {

}