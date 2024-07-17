package my.com.liberty.apigateway.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@NoArgsConstructor
@Setter
@Getter
public class SignDocumentRequest {
    private String environment;
    private Map<String, Object> document;
}
