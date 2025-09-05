# FHIR JSON Validation Test Suite

A comprehensive ITB test suite for validating the custom FHIR JSON validator plugin according to GITB TDL specifications.

## 📁 Structure

```
itb-test-suite/
├── testSuite.xml                    # Main test suite definition
├── testCases/                       # Individual test case files
│   ├── validate-fhir-patient-basic.xml
│   ├── validate-fhir-patient-with-ig.xml
│   ├── validate-fhir-patient-with-profile.xml
│   ├── validate-fhir-patient-with-ig-and-profile.xml
│   ├── validate-invalid-json.xml
│   └── validate-non-fhir-json.xml
├── resources/                        # Test data files
│   ├── patient-basic.json          # Valid FHIR Patient resource
│   ├── invalid-json.json           # Malformed JSON
│   └── non-fhir.json               # Valid JSON, not FHIR
├── docs/                            # Documentation
│   └── test-suite-documentation.html
└── README.md                        # This file
```

## 🚀 Usage

### Import to ITB

1. **Zip the entire `itb-test-suite` folder** (not a subfolder)
2. **Import the ZIP** into your ITB instance via Test Suite management
3. **The test suite will be automatically deployed** with all test cases and actors

### Test Cases Overview

| Test Case | Purpose | Expected Result | Custom Parameters |
|-----------|---------|-----------------|-------------------|
| Basic Patient Validation | Validates FHIR Patient without constraints | SUCCESS | None |
| With Implementation Guide | Tests IG-specific validation | SUCCESS | `ig=hl7.fhir.r4.core` |
| With Profile | Tests profile-based validation | SUCCESS | `profile=http://hl7.org/fhir/StructureDefinition/Patient` |
| With IG + Profile | Tests combined IG and profile validation | SUCCESS | Both parameters |
| Invalid JSON | Tests error handling for malformed JSON | FAILURE | None |
| Non-FHIR JSON | Tests error handling for non-FHIR content | FAILURE | None |

## 🔧 Prerequisites

- ✅ **Plugin deployed** to domain `fhir-validation`
- ✅ **Domain configured** with plugin in `config.properties`
- ✅ **ITB service running** and accessible

## 📊 Expected Results

### Success Cases
- **Result**: SUCCESS
- **Reports**: JSON successfully parsed as valid FHIR resource
- **Additional Info**: Implementation Guide and/or Profile specified (if provided)

### Failure Cases
- **Result**: FAILURE
- **Reports**: Specific error messages from the plugin
- **Examples**: JSON parsing errors, missing resourceType, validation failures

## 🎯 Custom Parameters

The validator plugin supports these custom parameters:

- **`ig`**: Implementation guide identifier
  - Example: `hl7.fhir.r4.core`
  - Purpose: Apply IG-specific validation rules

- **`profile`**: Profile URL for validation
  - Example: `http://hl7.org/fhir/StructureDefinition/Patient`
  - Purpose: Validate against specific FHIR profiles

## 📚 Standard ITB Inputs

These inputs are automatically provided by ITB:

- **`contentToValidate`**: File path to content being validated
- **`domain`**: Domain identifier
- **`validationType`**: Type of validation
- **`tempFolder`**: Temporary folder for processing
- **`locale`**: Locale for error messages

## 🔍 Test Data Files

### `patient-basic.json`
- **Type**: Valid FHIR R4 Patient resource
- **Content**: Complete patient with identifiers, names, contact info, address
- **Use**: All positive test cases

### `invalid-json.json`
- **Type**: Malformed JSON (missing comma)
- **Content**: FHIR-like structure with syntax error
- **Use**: Test JSON parsing error handling

### `non-fhir.json`
- **Type**: Valid JSON, not FHIR resource
- **Content**: Generic person data without resourceType
- **Use**: Test FHIR resource validation

## 📖 Documentation

See `docs/test-suite-documentation.html` for detailed HTML documentation with styling and examples.

## 🚨 Troubleshooting

### Import Issues
- Ensure you're zipping the `itb-test-suite` folder itself, not its contents
- Verify all XML files are valid and well-formed
- Check that actor endpoints match your ITB configuration

### Test Execution Issues
- Verify the plugin is deployed and accessible
- Check domain configuration in `config.properties`
- Ensure test data files are accessible

## 📚 References

- **GITB TDL Documentation**: [Test Suite Specification](https://www.itb.ec.europa.eu/docs/tdl/latest/testsuite/index.html)
- **ITB User Guide**: [Test Suite Management](https://www.itb.ec.europa.eu/docs/itb-ta/latest/)
- **FHIR Resources**: [HL7 FHIR](https://www.hl7.org/fhir/)

---

**Note**: This test suite follows GITB TDL v1 specifications and should import successfully into any compliant ITB instance.

