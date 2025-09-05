package se.oskar.fhir.plugin;

import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PluginRunner {
  public static void main(String[] args) {
    try {
      String projectRoot = Path.of(".").toAbsolutePath().normalize().toString();
      Path sample = Path.of(projectRoot, "..", "fhir-r4-validation-service", "itb-test-suites", "fhir-r4", "resources", "patient.json").normalize();
      Map<String, Object> input = new HashMap<>();
      input.put("contentToValidate", sample.toString());
      ValidateRequest request = new ValidateRequest(input);

      FhirJsonValidatorPlugin plugin = new FhirJsonValidatorPlugin();
      ValidationResponse response = plugin.validate(request);

      System.out.println("Result: " + response.getReport().getResult());
      response.getReport().getReports().forEach(r -> {
        System.out.println(r.getType() + ": " + r.getDescription() + " @ " + r.getLocation());
      });
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}




