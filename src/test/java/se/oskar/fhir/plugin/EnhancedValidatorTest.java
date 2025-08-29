package se.oskar.fhir.plugin;

import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;
import com.gitb.types.v1.TestResultType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class EnhancedValidatorTest {

    private FhirJsonValidatorPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new FhirJsonValidatorPlugin();
    }

    @Test
    void testFilePathValidation() {
        // Test with file path (existing functionality)
        Map<String, Object> input = new HashMap<>();
        input.put("contentToValidate", "../fhir-r4-validation-service/itb-test-suites/fhir-r4/resources/patient.json");
        
        ValidateRequest request = new ValidateRequest(input);
        ValidationResponse response = plugin.validate(request);
        
        assertEquals(TestResultType.SUCCESS, response.getReport().getResult());
    }

    @Test
    void testBase64Validation() {
        // Test with base64 encoded JSON
        String jsonContent = "{\"resourceType\":\"Patient\",\"id\":\"example\",\"name\":[{\"family\":\"Doe\",\"given\":[\"John\"]}],\"gender\":\"male\",\"birthDate\":\"1980-01-01\"}";
        String base64Content = java.util.Base64.getEncoder().encodeToString(jsonContent.getBytes());
        
        Map<String, Object> input = new HashMap<>();
        input.put("contentToValidate", base64Content);
        input.put("contentType", "application/fhir+json");
        
        ValidateRequest request = new ValidateRequest(input);
        ValidationResponse response = plugin.validate(request);
        
        assertEquals(TestResultType.SUCCESS, response.getReport().getResult());
    }

    @Test
    void testRawJsonValidation() {
        // Test with raw JSON string
        String jsonContent = "{\"resourceType\":\"Patient\",\"id\":\"example\",\"name\":[{\"family\":\"Doe\",\"given\":[\"John\"]}],\"gender\":\"male\",\"birthDate\":\"1980-01-01\"}";
        
        Map<String, Object> input = new HashMap<>();
        input.put("contentToValidate", jsonContent);
        input.put("contentType", "application/fhir+json");
        
        ValidateRequest request = new ValidateRequest(input);
        ValidationResponse response = plugin.validate(request);
        
        assertEquals(TestResultType.SUCCESS, response.getReport().getResult());
    }

    @Test
    void testWithIGAndProfile() {
        // Test with IG and profile parameters
        String jsonContent = "{\"resourceType\":\"Patient\",\"id\":\"example\",\"name\":[{\"family\":\"Doe\",\"given\":[\"John\"]}],\"gender\":\"male\",\"birthDate\":\"1980-01-01\"}";
        
        Map<String, Object> input = new HashMap<>();
        input.put("contentToValidate", jsonContent);
        input.put("contentType", "application/fhir+json");
        input.put("ig", "http://hl7.org/fhir/us/core/ImplementationGuide/hl7.fhir.us.core");
        input.put("profile", "http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient");
        
        ValidateRequest request = new ValidateRequest(input);
        ValidationResponse response = plugin.validate(request);
        
        assertEquals(TestResultType.SUCCESS, response.getReport().getResult());
        
        // Verify that IG and profile info is included in reports
        boolean hasIGInfo = response.getReport().getReports().stream()
            .anyMatch(r -> r.getDescription().contains("Implementation Guide specified"));
        boolean hasProfileInfo = response.getReport().getReports().stream()
            .anyMatch(r -> r.getDescription().contains("Profile specified"));
        
        assertTrue(hasIGInfo, "Should include IG information");
        assertTrue(hasProfileInfo, "Should include profile information");
    }

    @Test
    void testInvalidContentType() {
        // Test with invalid content type
        String jsonContent = "{\"resourceType\":\"Patient\",\"id\":\"example\"}";
        
        Map<String, Object> input = new HashMap<>();
        input.put("contentToValidate", jsonContent);
        input.put("contentType", "text/plain");
        
        ValidateRequest request = new ValidateRequest(input);
        ValidationResponse response = plugin.validate(request);
        
        assertEquals(TestResultType.FAILURE, response.getReport().getResult());
    }

    @Test
    void testInvalidJson() {
        // Test with invalid JSON
        String invalidJson = "{\"resourceType\":\"Patient\",\"id\":\"example\",\"invalidField\":{\"nested\":\"value\"\"missingComma\":true}}";
        
        Map<String, Object> input = new HashMap<>();
        input.put("contentToValidate", invalidJson);
        input.put("contentType", "application/fhir+json");
        
        ValidateRequest request = new ValidateRequest(input);
        ValidationResponse response = plugin.validate(request);
        
        assertEquals(TestResultType.FAILURE, response.getReport().getResult());
    }
}
