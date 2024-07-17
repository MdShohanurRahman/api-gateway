package my.com.liberty.apigateway.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class SignDocumentResponse {
    private String environment;
    private JsonNode document;
}
