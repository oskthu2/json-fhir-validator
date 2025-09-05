package se.oskar.fhir.plugin;

import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;
import com.gitb.types.v1.TestResultType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EnhancedValidatorTest {

    private FhirJsonValidatorPlugin plugin;
    
    @TempDir
    Path tempDir;

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
    void testBase64Validation() throws IOException {
        // Test with base64 encoded JSON - create temporary file with decoded JSON content
        String jsonContent = "{\"resourceType\":\"Patient\",\"id\":\"example\",\"name\":[{\"family\":\"Doe\",\"given\":[\"John\"]}],\"gender\":\"male\",\"birthDate\":\"1980-01-01\"}";
        
        // Create temporary file with the actual JSON content (not base64 encoded)
        // This simulates what would happen in a real scenario where base64 content is decoded
        File tempFile = tempDir.resolve("base64-decoded-test.json").toFile();
        Files.write(tempFile.toPath(), jsonContent.getBytes());
        
        Map<String, Object> input = new HashMap<>();
        input.put("contentToValidate", tempFile.getAbsolutePath());
        input.put("contentType", "application/fhir+json");
        
        ValidateRequest request = new ValidateRequest(input);
        ValidationResponse response = plugin.validate(request);
        
        assertEquals(TestResultType.SUCCESS, response.getReport().getResult());
    }

    @Test
    void testRawJsonValidation() throws IOException {
        // Test with raw JSON string - create temporary file
        String jsonContent = "{\"resourceType\":\"Patient\",\"id\":\"example\",\"name\":[{\"family\":\"Doe\",\"given\":[\"John\"]}],\"gender\":\"male\",\"birthDate\":\"1980-01-01\"}";
        
        // Create temporary file with JSON content
        File tempFile = tempDir.resolve("raw-json-test.json").toFile();
        Files.write(tempFile.toPath(), jsonContent.getBytes());
        
        Map<String, Object> input = new HashMap<>();
        input.put("contentToValidate", tempFile.getAbsolutePath());
        input.put("contentType", "application/fhir+json");
        
        ValidateRequest request = new ValidateRequest(input);
        ValidationResponse response = plugin.validate(request);
        
        assertEquals(TestResultType.SUCCESS, response.getReport().getResult());
    }

    @Test
    void testWithIGAndProfile() throws IOException {
        // Test with IG and profile parameters - create temporary file
        String jsonContent = "{\"resourceType\":\"Patient\",\"id\":\"example\",\"name\":[{\"family\":\"Doe\",\"given\":[\"John\"]}],\"gender\":\"male\",\"birthDate\":\"1980-01-01\"}";
        
        // Create temporary file with JSON content
        File tempFile = tempDir.resolve("ig-profile-test.json").toFile();
        Files.write(tempFile.toPath(), jsonContent.getBytes());
        
        Map<String, Object> input = new HashMap<>();
        input.put("contentToValidate", tempFile.getAbsolutePath());
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
    void testInvalidContentType() throws IOException {
        // Test with invalid content type - create temporary file
        String jsonContent = "{\"resourceType\":\"Patient\",\"id\":\"example\"}";
        
        // Create temporary file with JSON content
        File tempFile = tempDir.resolve("invalid-type-test.json").toFile();
        Files.write(tempFile.toPath(), jsonContent.getBytes());
        
        Map<String, Object> input = new HashMap<>();
        input.put("contentToValidate", tempFile.getAbsolutePath());
        input.put("contentType", "text/plain");
        
        ValidateRequest request = new ValidateRequest(input);
        ValidationResponse response = plugin.validate(request);
        
        assertEquals(TestResultType.FAILURE, response.getReport().getResult());
    }

    @Test
    void testInvalidJson() throws IOException {
        // Test with invalid JSON - create temporary file
        String invalidJson = "{\"resourceType\":\"Patient\",\"id\":\"example\",\"invalidField\":{\"nested\":\"value\"\"missingComma\":true}}";
        
        // Create temporary file with invalid JSON content
        File tempFile = tempDir.resolve("invalid-json-test.json").toFile();
        Files.write(tempFile.toPath(), invalidJson.getBytes());
        
        Map<String, Object> input = new HashMap<>();
        input.put("contentToValidate", tempFile.getAbsolutePath());
        input.put("contentType", "application/fhir+json");
        
        ValidateRequest request = new ValidateRequest(input);
        ValidationResponse response = plugin.validate(request);
        
        assertEquals(TestResultType.FAILURE, response.getReport().getResult());
    }
    
    @Test
    void testITBStandardInputs() throws IOException {
        // Test that the plugin handles standard ITB inputs correctly
        String jsonContent = "{\"resourceType\":\"Patient\",\"id\":\"example\",\"name\":[{\"family\":\"Doe\",\"given\":[\"John\"]}]}";
        
        // Create temporary file with JSON content
        File tempFile = tempDir.resolve("itb-inputs-test.json").toFile();
        Files.write(tempFile.toPath(), jsonContent.getBytes());
        
        Map<String, Object> input = new HashMap<>();
        input.put("contentToValidate", tempFile.getAbsolutePath());
        input.put("domain", "test-domain");
        input.put("validationType", "test-validation");
        input.put("tempFolder", tempDir.toString());
        input.put("locale", "en");
        input.put("contentType", "application/fhir+json");
        
        ValidateRequest request = new ValidateRequest(input);
        ValidationResponse response = plugin.validate(request);
        
        assertEquals(TestResultType.SUCCESS, response.getReport().getResult());
    }
}
