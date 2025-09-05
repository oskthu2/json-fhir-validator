# GITB Administrator Guide: FHIR JSON Validator Plugin

This guide explains how to import and configure the FHIR JSON Validator Plugin in your GITB Test Bed for FHIR resource validation. This plugin fully conforms to the [ITB Guide for Creating Custom Validator Plugins](https://www.itb.ec.europa.eu/docs/guides/latest/creatingCustomValidatorPlugin/index.html).

## 📦 **1. Deploying the Plugin**

### **Step 1: Build the Plugin**
```bash
# Clone and build the plugin
git clone <repository-url>
cd json-fhir-validator
mvn clean package

# The plugin JAR will be created at:
# target/json-fhir-validator-0.1.0-SNAPSHOT-shaded.jar
```

### **Step 2: Deploy to Validator Domain**
1. **Access your GITB domain** → **Specifications** → **Validators**
2. **Select your JSON validator** (or create one if it doesn't exist)
3. **Go to Domain Configuration** → **Resources**
4. **Create a `plugins` folder** in the domain's resources
5. **Upload the plugin JAR** to the `plugins` folder
6. **Configure the plugin** in the domain's `config.properties`

### **Step 3: Configure Plugin in Domain**
Add this to your domain's `config.properties`:
```properties
# Plugin configuration
validator.defaultPlugins.0.jar = plugins/json-fhir-validator-0.1.0-SNAPSHOT-shaded.jar
validator.defaultPlugins.0.class = se.oskar.fhir.plugin.FhirJsonValidatorPlugin
```

### **Step 4: Verify Plugin Registration**
- Restart the validator service
- Check validator logs for plugin loading confirmation
- Plugin should now be available for all validation types in that domain

## ⚙️ **2. Configuring Plugin Parameters**

### **ITB Standard Inputs**
The plugin automatically handles these standard ITB inputs (no configuration needed):
- **`contentToValidate`**: File path to FHIR JSON content (automatically provided by ITB validator)
- **`domain`**: Validation domain identifier (automatically provided by ITB)
- **`validationType`**: Type of validation being performed (automatically provided by ITB)
- **`tempFolder`**: Temporary folder for plugin operations (automatically provided by ITB)
- **`locale`**: Language for validation messages (automatically provided by ITB)

### **Custom FHIR Parameters**
Create these parameters in your **Actor Configuration**:

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

## 🧪 **3. How Plugins Work in GITB**

### **Plugin Integration Architecture**
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Test Case     │───▶│  JSON Validator  │───▶│ FHIR Plugin     │
│                 │    │                  │    │                 │
│ - Upload file   │    │ - Core validation│    │ - FHIR parsing  │
│ - Call validator│    │ - Schema check   │    │ - IG/Profile    │
│ - Get results   │    │ - Plugin calls   │    │ - Custom logic  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### **Important: Plugins are NOT called directly from tests**
- **Plugins extend validators**, they don't replace them
- **Tests call the validator**, which then calls the plugin
- **Plugin results are merged** with validator results automatically

### **What Happens When You Run a Test**
1. **Test uploads FHIR JSON file** to the JSON validator
2. **JSON validator performs core validation** (JSON syntax, schema if configured)
3. **JSON validator automatically calls the FHIR plugin** with the same content
4. **Plugin adds FHIR-specific validation** (resource parsing, IG/profile info)
5. **All results are combined** into a single validation report
6. **Test receives the complete report** with both validator and plugin findings

## 🧪 **4. Creating Tests That Use the Plugin**

### **Basic Test Case Example**
```xml
<testcase id="validate-fhir-patient" name="Validate FHIR Patient Resource">
  <steps>
    <step id="step1" name="Upload FHIR JSON File">
      <upload id="patientFile" source="file:patient.json"/>
    </step>
    
    <step id="step2" name="Call JSON Validator">
      <call id="validator" operation="validate">
        <input name="contentToValidate" source="patientFile"/>
      </call>
    </step>
    
    <step id="step3" name="Check Combined Results">
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
    <step id="step1" name="Upload FHIR JSON File">
      <upload id="patientFile" source="file:patient.json"/>
    </step>
    
    <step id="step2" name="Call JSON Validator with IG/Profile">
      <call id="validator" operation="validate">
        <input name="contentToValidate" source="patientFile"/>
        <input name="ig" value="http://hl7.org/fhir/us/core/ImplementationGuide/hl7.fhir.us.core"/>
        <input name="profile" value="http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient"/>
      </call>
    </step>
    
    <step id="step3" name="Check Combined Validation Result">
      <verify id="result" handler="ResultVerificationHandler">
        <input name="actual" source="validator"/>
        <input name="expected" value="SUCCESS"/>
      </verify>
    </step>
    
    <step id="step4" name="Verify Plugin-Specific Information">
      <verify id="reports" handler="ReportVerificationHandler">
        <input name="actual" source="validator"/>
        <input name="expectedIG" value="Implementation Guide specified: http://hl7.org/fhir/us/core/ImplementationGuide/hl7.fhir.us.core"/>
        <input name="expectedProfile" value="Profile specified: http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient"/>
      </verify>
    </step>
  </steps>
</testcase>
```

### **Key Points About Plugin Usage**
- **No test suite wrapper needed** - plugins are deployed to validators, not as test suites
- **Tests call the validator normally** - the plugin is automatically invoked
- **Plugin results appear automatically** - no special test configuration required
- **All validation happens in one call** - JSON validation + FHIR validation combined

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

## 🔄 **5. Complete Deployment Workflow**

### **Step-by-Step Deployment**
```bash
# 1. Build the plugin
mvn clean package

# 2. Deploy to GITB validator domain
# - Upload JAR to domain/resources/plugins/
# - Configure config.properties
# - Restart validator service

# 3. Create test cases
# - Upload FHIR JSON files
# - Call JSON validator
# - Plugin automatically runs
# - Get combined results
```

### **File Structure After Deployment**
```
validator-domain/
├── resources/
│   ├── plugins/
│   │   └── json-fhir-validator-0.1.0-SNAPSHOT-shaded.jar
│   ├── schemas/          # JSON schemas (if any)
│   └── config.properties # Domain configuration
└── config.properties     # Plugin configuration
```

### **Configuration Files**
**Domain config.properties:**
```properties
# Domain settings
domain.name = fhir-validation
domain.description = FHIR JSON validation with custom plugin

# Plugin configuration
validator.defaultPlugins.0.jar = plugins/json-fhir-validator-0.1.0-SNAPSHOT-shaded.jar
validator.defaultPlugins.0.class = se.oskar.fhir.plugin.FhirJsonValidatorPlugin
```

## 📚 **6. Additional Resources**

- **Plugin Documentation**: See `README.md` for detailed technical information
- **Demo Application**: Run `mvn exec:java@run-runner` to see examples
- **Test Suite Examples**: Check the `itb-test-suites` directory
- **GITB Documentation**: [ITB User Guide](https://www.itb.ec.europa.eu/docs/itb-ta/latest/)
- **ITB Plugin Guide**: [Creating Custom Validator Plugins](https://www.itb.ec.europa.eu/docs/guides/latest/creatingCustomValidatorPlugin/index.html)

---

**Note**: This plugin provides basic FHIR JSON validation. For advanced validation features including profile validation, terminology checking, and implementation guide compliance, consider using the full `fhir-r4-validation-service` project.
