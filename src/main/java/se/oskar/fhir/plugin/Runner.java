package se.oskar.fhir.plugin;

import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Runner {
  public static void main(String[] args) {
    try {
      System.out.println("=== FHIR JSON Validator Enhanced Demo ===\n");
      
      FhirJsonValidatorPlugin plugin = new FhirJsonValidatorPlugin();
      
      // Demo 1: File path validation (existing functionality)
      System.out.println("1. Testing file path validation:");
      String projectRoot = Path.of(".").toAbsolutePath().normalize().toString();
      Path sample = Path.of(projectRoot, "..", "fhir-r4-validation-service", "itb-test-suites", "fhir-r4", "resources", "patient.json").normalize();
      testValidation(plugin, "File Path", sample.toString(), null, null, null);
      
      // Demo 2: Raw JSON validation
      System.out.println("\n2. Testing raw JSON validation:");
      String rawJson = "{\"resourceType\":\"Patient\",\"id\":\"example\",\"name\":[{\"family\":\"Doe\",\"given\":[\"John\"]}],\"gender\":\"male\",\"birthDate\":\"1980-01-01\"}";
      testValidation(plugin, "Raw JSON", rawJson, "application/fhir+json", null, null);
      
      // Demo 3: Base64 validation
      System.out.println("\n3. Testing base64 validation:");
      String base64Json = java.util.Base64.getEncoder().encodeToString(rawJson.getBytes());
      testValidation(plugin, "Base64", base64Json, "application/fhir+json", null, null);
      
      // Demo 4: With IG and profile
      System.out.println("\n4. Testing with IG and profile:");
      testValidation(plugin, "IG+Profile", rawJson, "application/fhir+json", 
          "http://hl7.org/fhir/us/core/ImplementationGuide/hl7.fhir.us.core",
          "http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient");
      
      // Demo 5: Invalid content type
      System.out.println("\n5. Testing invalid content type:");
      testValidation(plugin, "Invalid Type", rawJson, "text/plain", null, null);
      
      System.out.println("\n=== Demo completed successfully ===");
      
    } catch (Exception e) {
      System.err.println("Demo failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  private static void testValidation(FhirJsonValidatorPlugin plugin, String testName, Object content, 
                                   String contentType, String ig, String profile) {
    try {
      Map<String, Object> input = new HashMap<>();
      input.put("contentToValidate", content);
      if (contentType != null) {
        input.put("contentType", contentType);
      }
      if (ig != null) {
        input.put("ig", ig);
      }
      if (profile != null) {
        input.put("profile", profile);
      }
      
      ValidateRequest request = new ValidateRequest(input);
      ValidationResponse response = plugin.validate(request);

      System.out.println("   " + testName + " - Result: " + response.getReport().getResult());
      response.getReport().getReports().forEach(r -> {
        System.out.println("     " + r.getType() + ": " + r.getDescription());
      });
    } catch (Exception e) {
      System.out.println("   " + testName + " - Error: " + e.getMessage());
    }
  }
}


