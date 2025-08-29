# GITB Administrator Guide: FHIR JSON Validator Plugin

This guide explains how to import and configure the FHIR JSON Validator Plugin in your GITB Test Bed for FHIR resource validation.

## 📦 **1. Importing the Plugin**

### **Step 1: Build the Plugin**
```bash
# Clone and build the plugin
git clone <repository-url>
cd json-fhir-validator
mvn clean package

# The plugin JAR will be created at:
# target/json-fhir-validator-0.1.0-SNAPSHOT-shaded.jar
```

### **Step 2: Upload to GITB**
1. **Access your GITB domain** → **Specifications** → **Test suites**
2. **Click "Upload test suite"**
3. **Select the plugin JAR**: `json-fhir-validator-0.1.0-SNAPSHOT-shaded.jar`
4. **Upload and deploy** the test suite

### **Step 3: Verify Plugin Registration**
- The plugin should appear in your test suite list
- Check that the main class `se.oskar.fhir.plugin.FhirJsonValidatorPlugin` is recognized

## ⚙️ **2. Configuring Plugin Parameters**

### **Required Parameters**
Create these parameters in your **Actor Configuration**:

| Parameter Name | Key | Type | Required | Default Value | Description |
|----------------|-----|------|----------|---------------|-------------|
| Content to Validate | `contentToValidate` | Binary | Yes | - | FHIR JSON file or content |
| Content Type | `contentType` | Simple | No | `application/fhir+json` | MIME type of content |

### **Optional Parameters**
| Parameter Name | Key | Type | Required | Default Value | Description |
|----------------|-----|------|----------|---------------|-------------|
| Implementation Guide | `ig` | Simple | No | - | IG URL for validation context |
| Profile | `profile` | Simple | No | - | Profile URL for specific rules |

### **Parameter Configuration Steps**
1. **Go to Actor Details** → **Configuration parameters**
2. **Click "Create parameter"** for each parameter
3. **Set properties**:
   - ✅ **Included in tests**: Check this for all parameters
   - ✅ **Required**: Check only for `contentToValidate`
   - ✅ **Editable**: Check for user-configurable parameters

## 🧪 **3. Invoking the Plugin from Tests**

### **Basic Test Case Example**
```xml
<testcase id="validate-fhir-patient" name="Validate FHIR Patient Resource">
  <steps>
    <step id="step1" name="Validate FHIR JSON">
      <call id="validator" operation="validate">
        <input name="contentToValidate" source="file:patient.json"/>
        <input name="contentType" value="application/fhir+json"/>
      </call>
    </step>
    
    <step id="step2" name="Check Validation Result">
      <verify id="result" handler="ResultVerificationHandler">
        <input name="actual" source="validator"/>
        <input name="expected" value="SUCCESS"/>
      </verify>
    </step>
  </steps>
</testcase>
```

### **Advanced Test Case with IG and Profile**
```xml
<testcase id="validate-fhir-patient-us-core" name="Validate FHIR Patient with US Core Profile">
  <steps>
    <step id="step1" name="Validate FHIR JSON with US Core Profile">
      <call id="validator" operation="validate">
        <input name="contentToValidate" source="file:patient.json"/>
        <input name="contentType" value="application/fhir+json"/>
        <input name="ig" value="http://hl7.org/fhir/us/core/ImplementationGuide/hl7.fhir.us.core"/>
        <input name="profile" value="http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient"/>
      </call>
    </step>
    
    <step id="step2" name="Check Validation Result">
      <verify id="result" handler="ResultVerificationHandler">
        <input name="actual" source="validator"/>
        <input name="expected" value="SUCCESS"/>
      </verify>
    </step>
    
    <step id="step3" name="Verify IG and Profile Information">
      <verify id="reports" handler="ReportVerificationHandler">
        <input name="actual" source="validator"/>
        <input name="expectedIG" value="Implementation Guide specified: http://hl7.org/fhir/us/core/ImplementationGuide/hl7.fhir.us.core"/>
        <input name="expectedProfile" value="Profile specified: http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient"/>
      </verify>
    </step>
  </steps>
</testcase>
```

### **Dynamic Parameter Values**
```xml
<!-- Using parameter values from GITB configuration -->
<call id="validator" operation="validate">
  <input name="contentToValidate" source="file:${contentFile}"/>
  <input name="contentType" value="${contentType}"/>
  <input name="ig" value="${implementationGuide}"/>
  <input name="profile" value="${profile}"/>
</call>
```

## 📊 **4. Understanding Plugin Responses**

### **Success Response**
```xml
<tar>
  <result>SUCCESS</result>
  <reports>
    <item>
      <type>INFO</type>
      <description>JSON successfully parsed as valid FHIR resource</description>
      <location>contentToValidate:0:0</location>
    </item>
    <item>
      <type>INFO</type>
      <description>Implementation Guide specified: [IG_URL]</description>
      <location>ig:0:0</location>
    </item>
    <item>
      <type>INFO</type>
      <description>Profile specified: [PROFILE_URL]</description>
      <location>profile:0:0</location>
    </item>
  </reports>
</tar>
```

### **Error Response**
```xml
<tar>
  <result>FAILURE</result>
  <reports>
    <item>
      <type>ERROR</type>
      <description>Failed to parse JSON as FHIR resource: [ERROR_DETAILS]</description>
      <location>contentToValidate:0:0</location>
    </item>
  </reports>
</tar>
```

## 🔧 **5. Troubleshooting**

### **Common Issues**
1. **Plugin not found**: Ensure the JAR is properly uploaded and deployed
2. **Parameter errors**: Verify all required parameters are configured
3. **Content type errors**: Ensure `contentType` is `application/fhir+json`
4. **File not found**: Check file paths in test cases

### **Validation Tips**
- **Test with simple FHIR resources** first before complex ones
- **Use the demo application** to verify plugin functionality
- **Check GITB logs** for detailed error information
- **Verify parameter values** are correctly passed to the plugin

## 📚 **6. Additional Resources**

- **Plugin Documentation**: See `README.md` for detailed technical information
- **Demo Application**: Run `mvn exec:java@run-runner` to see examples
- **Test Suite Examples**: Check the `itb-test-suites` directory
- **GITB Documentation**: [ITB User Guide](https://www.itb.ec.europa.eu/docs/itb-ta/latest/)

---

**Note**: This plugin provides basic FHIR JSON validation. For advanced validation features including profile validation, terminology checking, and implementation guide compliance, consider using the full `fhir-r4-validation-service` project.
