package my.com.liberty.apigateway.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class SignDocumentResponse {
    private String environment;
    private String document;
}
