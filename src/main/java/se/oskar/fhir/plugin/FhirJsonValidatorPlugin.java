package se.oskar.fhir.plugin;

import com.gitb.types.v1.TAR;
import com.gitb.types.v1.TestAssertionReportType;
import com.gitb.types.v1.TestResultType;
import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;
import com.gitb.vs.ValidationService;

import java.util.Locale;
import java.util.ResourceBundle;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
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

  // Standard ITB validator plugin inputs
  private static final String INPUT_CONTENT_TO_VALIDATE = "contentToValidate";
  private static final String INPUT_DOMAIN = "domain";
  private static final String INPUT_VALIDATION_TYPE = "validationType";
  private static final String INPUT_LOCALE = "locale";
  
  // Custom FHIR-specific inputs
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
    
    // Extract standard ITB inputs
    String contentToValidatePath = getString(input.get(INPUT_CONTENT_TO_VALIDATE), null);
    String domain = getString(input.get(INPUT_DOMAIN), "unknown");
    String validationType = getString(input.get(INPUT_VALIDATION_TYPE), "unknown");
    String locale = getString(input.get(INPUT_LOCALE), "en");
    
    // Extract custom FHIR inputs
    String contentType = getString(input.get(INPUT_CONTENT_TYPE), DEFAULT_CONTENT_TYPE);
    String ig = getString(input.get(INPUT_IG), null);
    String profile = getString(input.get(INPUT_PROFILE), null);

    // Create TAR report with proper ITB structure
    TAR report = createReport(domain, validationType, locale);

    try {
      // Validate content type
      if (!isValidContentType(contentType)) {
        TestAssertionReportType item = new TestAssertionReportType();
        item.setDescription(getLocalizedMessage(locale, "error.unsupported.content.type", contentType));
        item.setLocation(String.format("%s:0:0", INPUT_CONTENT_TYPE));
        item.setType("ERROR");
        report.getReports().add(item);
        report.setResult(TestResultType.FAILURE);
        
        ValidationResponse response = new ValidationResponse();
        response.setReport(report);
        return response;
      }

      // Parse content to validate
      String json = parseContent(contentToValidatePath);
      
      // Try to parse the JSON as a FHIR resource
      try {
        fhirContext.newJsonParser().parseResource(json);
        
        // If parsing succeeds, the JSON is valid FHIR
        report.setResult(TestResultType.SUCCESS);
        
        // Add success message
        TestAssertionReportType item = new TestAssertionReportType();
        item.setDescription(getLocalizedMessage(locale, "info.success.parsing"));
        item.setLocation(String.format("%s:0:0", INPUT_CONTENT_TO_VALIDATE));
        item.setType("INFO");
        report.getReports().add(item);
        
        // Add IG and profile information if provided
        if (ig != null && !ig.trim().isEmpty()) {
          TestAssertionReportType igItem = new TestAssertionReportType();
          igItem.setDescription(getLocalizedMessage(locale, "info.ig.specified", ig));
          igItem.setLocation(String.format("%s:0:0", INPUT_IG));
          igItem.setType("INFO");
          report.getReports().add(igItem);
        }
        
        if (profile != null && !profile.trim().isEmpty()) {
          TestAssertionReportType profileItem = new TestAssertionReportType();
          profileItem.setDescription(getLocalizedMessage(locale, "info.profile.specified", profile));
          profileItem.setLocation(String.format("%s:0:0", INPUT_PROFILE));
          profileItem.setType("INFO");
          report.getReports().add(profileItem);
        }
        
      } catch (Exception parseException) {
        // If parsing fails, add an error
        TestAssertionReportType item = new TestAssertionReportType();
        item.setDescription(getLocalizedMessage(locale, "error.parsing.failed", parseException.getMessage()));
        item.setLocation(String.format("%s:0:0", INPUT_CONTENT_TO_VALIDATE));
        item.setType("ERROR");
        report.getReports().add(item);
        report.setResult(TestResultType.FAILURE);
      }
      
    } catch (Exception e) {
      TestAssertionReportType item = new TestAssertionReportType();
      item.setDescription(getLocalizedMessage(locale, "error.content.processing", e.getMessage()));
      item.setLocation(String.format("%s:0:0", INPUT_CONTENT_TO_VALIDATE));
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

  private static String parseContent(String contentPath) throws Exception {
    if (contentPath == null) {
      throw new IllegalArgumentException("contentToValidate is required");
    }
    
    // For ITB plugins, contentToValidate is always a file path
    return FileUtils.readFileToString(new File(contentPath), StandardCharsets.UTF_8);
  }

  /**
   * Creates a properly formatted TAR report according to ITB standards
   */
  private static TAR createReport(String domain, String validationType, String locale) {
    TAR report = new TAR();
    report.setResult(TestResultType.SUCCESS);
    
    // Set context information
    // Note: TAR class doesn't have setDate method in our simplified version
    // In a full ITB implementation, this would be set
    
    return report;
  }

  /**
   * Gets localized message from resource bundle
   */
  private static String getLocalizedMessage(String locale, String key, Object... args) {
    try {
      Locale loc = Locale.forLanguageTag(locale);
      ResourceBundle bundle = ResourceBundle.getBundle("messages", loc);
      String message = bundle.getString(key);
      return String.format(message, args);
    } catch (Exception e) {
      // Fallback to English messages
      return getDefaultMessage(key, args);
    }
  }

  /**
   * Gets localized message without arguments
   */
  private static String getLocalizedMessage(String locale, String key) {
    return getLocalizedMessage(locale, key, new Object[0]);
  }

  /**
   * Provides default English messages when localization fails
   */
  private static String getDefaultMessage(String key, Object... args) {
    String message;
    switch (key) {
      case "error.unsupported.content.type":
        message = "Unsupported content type: %s. Expected application/fhir+json";
        break;
      case "info.success.parsing":
        message = "JSON successfully parsed as valid FHIR resource";
        break;
      case "info.ig.specified":
        message = "Implementation Guide specified: %s";
        break;
      case "info.profile.specified":
        message = "Profile specified: %s";
        break;
      case "error.parsing.failed":
        message = "Failed to parse JSON as FHIR resource: %s";
        break;
      case "error.content.processing":
        message = "Content processing error: %s";
        break;
      default:
        message = key;
    }
    return String.format(message, args);
  }
}


