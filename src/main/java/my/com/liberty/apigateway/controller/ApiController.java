package my.com.liberty.apigateway.controller;

import lombok.RequiredArgsConstructor;
import my.com.liberty.apigateway.dto.SignDocumentRequest;
import my.com.liberty.apigateway.dto.SignDocumentResponse;
import my.com.liberty.apigateway.service.SignDocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.CertificateException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ApiController {

    private final SignDocumentService signDocumentService;

    @PostMapping("/sign-document")
    public ResponseEntity<SignDocumentResponse> signDocument(@RequestBody SignDocumentRequest request) {
        return ResponseEntity.ok(signDocumentService.signDocument(request));
    }
}
