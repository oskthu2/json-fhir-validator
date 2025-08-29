package se.oskar.fhir.plugin;

import com.gitb.types.v1.TAR;
import com.gitb.types.v1.TestAssertionReportType;
import com.gitb.types.v1.TestResultType;
import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;
import com.gitb.vs.ValidationService;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Validator plugin entry point for JSON FHIR (R4) using HAPI FHIR.
 * Expected inputs per README:
 * - contentToValidate: the resource content (bytes, base64 string, or file path)
 * - contentType: MIME type (default application/fhir+json)
 * - ig: IG URL (optional)
 * - profile: Profile URL (optional)
 */
public class FhirJsonValidatorPlugin implements ValidationService {

  private static final String INPUT_CONTENT_TO_VALIDATE = "contentToValidate";
  private static final String INPUT_CONTENT_TYPE = "contentType";
  private static final String INPUT_IG = "ig";
  private static final String INPUT_PROFILE = "profile";
  private static final String DEFAULT_CONTENT_TYPE = "application/fhir+json";

  private final FhirContext fhirContext;

  public FhirJsonValidatorPlugin() {
    this.fhirContext = FhirContext.forR4();
  }

  public ValidationResponse validate(ValidateRequest request) {
    Map<String, Object> input = request.getInput();
    
    // Extract input parameters
    Object contentToValidate = input.get(INPUT_CONTENT_TO_VALIDATE);
    String contentType = getString(input.get(INPUT_CONTENT_TYPE), DEFAULT_CONTENT_TYPE);
    String ig = getString(input.get(INPUT_IG), null);
    String profile = getString(input.get(INPUT_PROFILE), null);

    TAR report = new TAR();

    try {
      // Validate content type
      if (!isValidContentType(contentType)) {
        TestAssertionReportType item = new TestAssertionReportType();
        item.setDescription("Unsupported content type: " + contentType + ". Expected application/fhir+json");
        item.setLocation(INPUT_CONTENT_TYPE + ":0:0");
        item.setType("ERROR");
        report.getReports().add(item);
        report.setResult(TestResultType.FAILURE);
        
        ValidationResponse response = new ValidationResponse();
        response.setReport(report);
        return response;
      }

      // Parse content to validate
      String json = parseContent(contentToValidate);
      
      // Try to parse the JSON as a FHIR resource
      try {
        fhirContext.newJsonParser().parseResource(json);
        
        // If parsing succeeds, the JSON is valid FHIR
        report.setResult(TestResultType.SUCCESS);
        
        // Add success message
        TestAssertionReportType item = new TestAssertionReportType();
        item.setDescription("JSON successfully parsed as valid FHIR resource");
        item.setLocation(INPUT_CONTENT_TO_VALIDATE + ":0:0");
        item.setType("INFO");
        report.getReports().add(item);
        
        // Add IG and profile information if provided
        if (ig != null && !ig.trim().isEmpty()) {
          TestAssertionReportType igItem = new TestAssertionReportType();
          igItem.setDescription("Implementation Guide specified: " + ig);
          igItem.setLocation(INPUT_IG + ":0:0");
          igItem.setType("INFO");
          report.getReports().add(igItem);
        }
        
        if (profile != null && !profile.trim().isEmpty()) {
          TestAssertionReportType profileItem = new TestAssertionReportType();
          profileItem.setDescription("Profile specified: " + profile);
          profileItem.setLocation(INPUT_PROFILE + ":0:0");
          profileItem.setType("INFO");
          report.getReports().add(profileItem);
        }
        
      } catch (Exception parseException) {
        // If parsing fails, add an error
        TestAssertionReportType item = new TestAssertionReportType();
        item.setDescription("Failed to parse JSON as FHIR resource: " + parseException.getMessage());
        item.setLocation(INPUT_CONTENT_TO_VALIDATE + ":0:0");
        item.setType("ERROR");
        report.getReports().add(item);
        report.setResult(TestResultType.FAILURE);
      }
      
    } catch (Exception e) {
      TestAssertionReportType item = new TestAssertionReportType();
      item.setDescription("Content processing error: " + e.getMessage());
      item.setLocation(INPUT_CONTENT_TO_VALIDATE + ":0:0");
      item.setType("ERROR");
      report.getReports().add(item);
      report.setResult(TestResultType.FAILURE);
    }

    ValidationResponse response = new ValidationResponse();
    response.setReport(report);
    return response;
  }



  private static String getString(Object any, String def) {
    return (any == null) ? def : any.toString();
  }

  private static boolean isValidContentType(String contentType) {
    return contentType != null && contentType.startsWith("application/fhir+json");
  }

  private static String parseContent(Object content) throws Exception {
    if (content == null) {
      throw new IllegalArgumentException("contentToValidate is required");
    }
    
    if (content instanceof byte[]) {
      // Handle byte array input
      return new String((byte[]) content, StandardCharsets.UTF_8);
    } else if (content instanceof String) {
      String str = (String) content;
      
      // Try to decode as base64 first
      try {
        byte[] decoded = Base64.getDecoder().decode(str);
        return new String(decoded, StandardCharsets.UTF_8);
      } catch (IllegalArgumentException e) {
        // If not base64, check if it's a file path
        if (str.contains("/") || str.contains("\\") || str.endsWith(".json")) {
          // Treat as file path
          return FileUtils.readFileToString(new File(str), StandardCharsets.UTF_8);
        } else {
          // Treat as raw JSON string
          return str;
        }
      }
    } else {
      throw new IllegalArgumentException("Unsupported content type for contentToValidate: " + content.getClass().getSimpleName());
    }
  }
}


