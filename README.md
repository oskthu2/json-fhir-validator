# FHIR JSON Validator Plugin

A robust GITB validator plugin for JSON FHIR (R4) resources using HAPI FHIR. This plugin provides comprehensive validation capabilities with support for multiple input formats, content type validation, and implementation guide/profile specifications.

## 🚀 Features

- **Multiple Input Formats**: Support for file paths, raw JSON strings, base64 encoded content, and byte arrays
- **Content Type Validation**: Ensures proper FHIR content type (`application/fhir+json`)
- **Implementation Guide Support**: Optional IG URL specification for enhanced validation context
- **Profile Validation**: Optional profile URL specification for specific validation rules
- **Comprehensive Error Reporting**: Detailed validation results with location information
- **Backward Compatibility**: Maintains existing file path functionality
- **HAPI FHIR Integration**: Built on the industry-standard HAPI FHIR library

## 📋 Requirements

- Java 17 or higher
- Maven 3.6+
- HAPI FHIR 6.10.3

## 🏗️ Architecture

The plugin implements the **GITB validation service interface** and follows the [ITB Guide for Creating Custom Validator Plugins](https://www.itb.ec.europa.eu/docs/guides/latest/creatingCustomValidatorPlugin/index.html). It provides:

- **Input Processing**: Intelligent detection and parsing of different content types
- **FHIR Validation**: JSON parsing and basic FHIR resource validation
- **Result Reporting**: Structured validation reports with success/failure status
- **Error Handling**: Comprehensive error reporting for various failure scenarios

## 🔧 Installation

### Prerequisites
```bash
# Ensure Java 17+ is installed
java -version

# Ensure Maven is installed
mvn -version
```

### Build the Plugin
```bash
# Clone the repository
git clone <repository-url>
cd json-fhir-validator

# Build the project
mvn clean package

# The shaded JAR will be created in target/
# json-fhir-validator-0.1.0-SNAPSHOT-shaded.jar
```

## 📖 Usage

### Basic Usage

The plugin accepts the following input parameters:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `contentToValidate` | Object | Yes | FHIR content (file path, JSON string, base64, or bytes) |
| `contentType` | String | No | MIME type (default: `application/fhir+json`) |
| `ig` | String | No | Implementation Guide URL |
| `profile` | String | No | Profile URL |

### Input Format Examples

#### 1. File Path Validation
```java
Map<String, Object> input = new HashMap<>();
input.put("contentToValidate", "/path/to/patient.json");
input.put("contentType", "application/fhir+json");

ValidateRequest request = new ValidateRequest(input);
ValidationResponse response = plugin.validate(request);
```

#### 2. Raw JSON String
```java
Map<String, Object> input = new HashMap<>();
input.put("contentToValidate", "{\"resourceType\":\"Patient\",\"id\":\"example\"}");
input.put("contentType", "application/fhir+json");

ValidateRequest request = new ValidateRequest(input);
ValidationResponse response = plugin.validate(request);
```

#### 3. Base64 Encoded Content
```java
String jsonContent = "{\"resourceType\":\"Patient\",\"id\":\"example\"}";
String base64Content = Base64.getEncoder().encodeToString(jsonContent.getBytes());

Map<String, Object> input = new HashMap<>();
input.put("contentToValidate", base64Content);
input.put("contentType", "application/fhir+json");

ValidateRequest request = new ValidateRequest(input);
ValidationResponse response = plugin.validate(request);
```

#### 4. With Implementation Guide and Profile
```java
Map<String, Object> input = new HashMap<>();
input.put("contentToValidate", jsonContent);
input.put("contentType", "application/fhir+json");
input.put("ig", "http://hl7.org/fhir/us/core/ImplementationGuide/hl7.fhir.us.core");
input.put("profile", "http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient");

ValidateRequest request = new ValidateRequest(input);
ValidationResponse response = plugin.validate(request);
```

### Response Format

The plugin returns a `ValidationResponse` with a `TAR` (Test Assertion Report) containing:

- **Result**: `SUCCESS`, `WARNING`, or `FAILURE`
- **Reports**: Array of validation messages with:
  - **Type**: `INFO`, `WARNING`, or `ERROR`
  - **Description**: Detailed message about the validation
  - **Location**: Source location of the validation issue

## 🧪 Testing

### Run Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EnhancedValidatorTest

# Run with detailed output
mvn test -Dtest=EnhancedValidatorTest -Dsurefire.useFile=false
```

### Demo Application
```bash
# Run the enhanced demo
mvn exec:java@run-runner
```

The demo showcases:
1. File path validation
2. Raw JSON validation
3. Base64 validation
4. IG and profile validation
5. Invalid content type handling

## 🔍 Validation Logic

### Content Type Validation
- Only accepts `application/fhir+json` content types
- Rejects unsupported types with clear error messages
- Defaults to `application/fhir+json` if not specified

### Content Processing
1. **Byte Arrays**: Direct UTF-8 conversion
2. **Strings**: 
   - Attempts base64 decoding first
   - Falls back to file path if contains path separators
   - Treats as raw JSON if neither
3. **File Paths**: Reads and validates file content

### FHIR Validation
- Parses JSON content using HAPI FHIR R4 context
- Validates basic FHIR resource structure
- Reports parsing errors with detailed messages

## 🚨 Error Handling

The plugin provides comprehensive error handling for:

- **Content Type Errors**: Unsupported MIME types
- **Content Processing Errors**: File reading, base64 decoding, etc.
- **FHIR Validation Errors**: JSON parsing, resource validation
- **Input Validation Errors**: Missing required parameters

## 📚 Dependencies

### Core Dependencies
- **HAPI FHIR Base**: Core FHIR functionality
- **HAPI FHIR R4 Structures**: R4 resource definitions
- **HAPI FHIR Validation**: Validation framework
- **Apache Commons IO**: File operations

### Test Dependencies
- **JUnit Jupiter**: Testing framework
- **Maven Surefire**: Test execution

## 🏭 Build Configuration

### Maven Plugins
- **Compiler Plugin**: Java 17 compilation
- **Shade Plugin**: Fat JAR creation with dependencies
- **Exec Plugin**: Demo application execution

### Shaded JAR
The build creates a self-contained JAR with all dependencies included, making it easy to deploy and use in various environments.

## 🔄 Integration

### ITB Compliance
This plugin fully conforms to the [ITB Guide for Creating Custom Validator Plugins](https://www.itb.ec.europa.eu/docs/guides/latest/creatingCustomValidatorPlugin/index.html) and implements:

- **Standard ITB Inputs**: `contentToValidate`, `domain`, `validationType`, `tempFolder`, `locale`
- **Proper TAR Reporting**: Structured validation reports with location information
- **Localization Support**: Multi-language message support via resource bundles
- **Plugin Architecture**: Follows ITB plugin loading and configuration patterns

### GITB Framework
This plugin is designed to integrate with the GITB (Generic Integration Testing Framework) and follows the [official ITB plugin standards](https://www.itb.ec.europa.eu/docs/guides/latest/creatingCustomValidatorPlugin/index.html). It can be used as:

- A standalone validation service
- A component in larger testing workflows
- A plugin in GITB-based test suites

### Customization
The plugin can be extended to support:
- Additional FHIR versions (DSTU2, STU3, R5)
- Custom validation rules
- Enhanced profile validation
- Terminology validation

## 📝 License

[Add your license information here]

## 🤝 Contributing

[Add contribution guidelines here]

## 📞 Support

[Add support contact information here]

## 🔗 Related Projects

- **fhir-r4-validation-service**: Full-featured FHIR validation service
- **HAPI FHIR**: The underlying FHIR library
- **GITB**: Generic Integration Testing Framework

---

**Note**: This plugin provides basic FHIR JSON validation. For advanced validation features including profile validation, terminology checking, and implementation guide compliance, consider using the full `fhir-r4-validation-service` project.
