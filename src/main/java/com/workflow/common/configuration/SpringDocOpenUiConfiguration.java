package com.workflow.common.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocOpenUiConfiguration {

    @Bean
    public OpenAPI workflowOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Workflow Operation API")
                .description("APIs for workflow CRUD plus entity-setting query and revision history.")
                .version("v1"));
    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }
            responses.putIfAbsent("500", new ApiResponse().description("Internal server error"));
            return operation;
        };
    }
}
