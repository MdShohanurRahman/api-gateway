package my.com.liberty.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.com.liberty.apigateway.dto.SignDocumentRequest;
import my.com.liberty.apigateway.dto.SignDocumentResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignDocumentService {

    private final ObjectMapper objectMapper;
    private final KeyPair selfSingKeyPair;
    private final X509Certificate selfSignCertificate;

    public SignDocumentResponse signDocument(SignDocumentRequest request) {
        // Prepare document data
        JsonNode rootNode = objectMapper.valueToTree(request.getDocument());
        String docString = minifyJson(rootNode);

        // Generate document hash
        byte[] docHash = sha256Hash(docString);
        String docDigest = encodeBase64(docHash);

        // Sign the document digest
        byte[] signHash = signData(docHash,selfSingKeyPair.getPrivate());
        String sign = encodeBase64(signHash);

        // Generate certificate hash
        byte[] certificateData = getCertificateData();
        byte[] certHash = sha256Hash(certificateData);
        String certDigest = encodeBase64(certHash);

        // Get certificate details
        String certSubject = selfSignCertificate.getSubjectX500Principal().getName();
        String certIssuerName = selfSignCertificate.getIssuerX500Principal().getName();
        BigInteger certSerialNumber = selfSignCertificate.getSerialNumber();

        // Generate signing time
        LocalDateTime currentDateTimeUTC = LocalDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String signingTime = currentDateTimeUTC.format(formatter);

        // Access specific nodes from sample json
        JsonNode sampleSignNode = getSampleJsonJson();

        JsonNode signatureInformationNode = sampleSignNode
                .path("UBLExtensions").get(0)
                .path("UBLExtension").get(0)
                .path("ExtensionContent").get(0)
                .path("UBLDocumentSignatures").get(0)
                .path("SignatureInformation").get(0);

        JsonNode signatureNode = signatureInformationNode.path("Signature").get(0);

        JsonNode x509DataNode = signatureNode
                .path("KeyInfo").get(0)
                .path("X509Data").get(0);

        JsonNode qualifyingPropertiesNode = signatureNode
                .path("Object").get(0)
                .path("QualifyingProperties").get(0);

        JsonNode signedSignaturePropertiesNode = qualifyingPropertiesNode
                .path("SignedProperties").get(0)
                .path("SignedSignatureProperties").get(0);

        JsonNode signingCertificateNode = signedSignaturePropertiesNode.path("SigningCertificate").get(0);
        JsonNode certNode = signingCertificateNode.path("Cert").get(0);
        JsonNode certDigestNode = certNode.path("CertDigest").get(0);
        JsonNode issuerSerialNode = certNode.path("IssuerSerial").get(0);


        // Populate the signed properties
        insertNodeValue(certDigestNode.path("DigestValue").get(0), certDigest);
        insertNodeValue(signedSignaturePropertiesNode.path("SigningTime").get(0), signingTime);
        insertNodeValue(issuerSerialNode.path("X509IssuerName").get(0), certIssuerName);
        insertNodeValue(issuerSerialNode.path("X509SerialNumber").get(0), certSerialNumber);

        // Generate signed properties hash
        String qualifyingProperties = minifyJson(qualifyingPropertiesNode);
        byte[] propsDigestHash = sha256Hash(qualifyingProperties);
        String propsDigest = encodeBase64(propsDigestHash);

        // Populate the information in the document to create the signed document
        JsonNode x509IssuerSerialNode = x509DataNode.path("X509IssuerSerial").get(0);
        JsonNode referenceNode = signatureNode.path("SignedInfo").get(0).path("Reference");

        insertNodeValue(signatureNode.path("SignatureValue").get(0), sign);
        insertNodeValue(x509DataNode.path("X509Certificate").get(0), encodeBase64(certificateData));
        insertNodeValue(x509DataNode.path("X509SubjectName").get(0), certSubject);
        insertNodeValue(x509IssuerSerialNode.path("X509IssuerName").get(0), certIssuerName);
        insertNodeValue(x509IssuerSerialNode.path("X509SerialNumber").get(0), certSerialNumber);
        insertNodeValue(referenceNode.get(0).path("DigestValue").get(0), propsDigest);
        insertNodeValue(referenceNode.get(1).path("DigestValue").get(0), docDigest);

        // Append UBLExtensions & Signature
        JsonNode invoiceNode = rootNode.path("Invoice");
        if (invoiceNode.isArray()) {
            ObjectNode invoiceNodeObject = (ObjectNode) invoiceNode.get(0);
            invoiceNodeObject.set("UBLExtensions", sampleSignNode.path("UBLExtensions"));
            invoiceNodeObject.set("Signature", sampleSignNode.path("Signature"));
        }

        SignDocumentResponse response = new SignDocumentResponse();
        response.setEnvironment(request.getEnvironment());
        response.setDocument(rootNode);

        return response;
    }

    private byte[] getCertificateData() {
        try {
            return selfSignCertificate.getEncoded();
        } catch (Exception ex) {
            throw new RuntimeException("getCertificateRawData error : " + ex.getMessage());
        }
    }

    private JsonNode getSampleJsonJson() {
        Resource resource = new ClassPathResource("json/sample-sign.json");
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        } catch (Exception ex) {
            throw new RuntimeException("failed to load json");
        }
        return jsonNode;
    }

    private void insertNodeValue(JsonNode node, String value) {
        if (node.isObject()) {
            ((ObjectNode) node).put("_", value);
        }
    }

    private void insertNodeValue(JsonNode node, BigInteger value) {
        if (node.isObject()) {
            ((ObjectNode) node).put("_", value);
        }
    }

    private String minifyJson(JsonNode json) {
        try {
            return objectMapper.writeValueAsString(json);
        } catch (Exception ex) {
            throw new RuntimeException("minifyJson exception");
        }
    }

    private byte[] sha256Hash(String data) {
        try {
            return sha256Hash(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new RuntimeException("sha256Hash exception");
        }
    }

    private byte[] sha256Hash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (Exception ex) {
            throw new RuntimeException("sha256Hash exception");
        }
    }

    private String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private byte[] signData(byte[] data, PrivateKey privateKey) {
        try {
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);
            signer.update(data);
            return signer.sign();
        } catch (Exception ex) {
            throw new RuntimeException("signData exception");
        }
    }

}
