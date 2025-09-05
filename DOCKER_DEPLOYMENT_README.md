# Docker ITB Deployment Guide

This guide explains how to deploy the FHIR JSON Validator Plugin to a Docker-based ITB instance using the automated deployment scripts.

## 🚀 **Quick Start**

### **1. Find Your ITB Containers**
```bash
# List running containers
docker ps

# Look for your ITB containers (should see 4 containers):
# - srv: Service/backend container (where plugins are deployed)
# - ui: User interface container
# - mysql: Database container
# - redis: Cache container
```

### **2. Customize Configuration**
Edit `docker-deploy.config` with your container details:
```bash
# The plugin deploys to the srv container (service container)
CONTAINER_NAME=srv
DOMAIN_NAME=fhir-validation
DOMAINS_PATH=/opt/itb/domains

# If your containers have different names, use the service container name
# Example: if your service container is named "itb-backend"
# CONTAINER_NAME=itb-backend
```

### **3. Run Deployment Script**

**Linux/macOS:**
```bash
# Make script executable
chmod +x deploy-to-docker-itb.sh

# Run deployment
./deploy-to-docker-itb.sh
```

**Windows PowerShell:**
```powershell
# Run deployment
.\deploy-to-docker-itb.ps1

# Or with custom parameters
.\deploy-to-docker-itb.ps1 -ContainerName "my-container" -DomainName "my-domain"
```

## 🏗️ **ITB Multi-Container Architecture**

ITB consists of four composed containers, each with a specific role:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│       UI        │    │      MySQL      │    │      Redis      │
│                 │    │                 │    │                 │
│ - Web interface │    │ - Database      │    │ - Caching       │
│ - User portal   │    │ - Test results  │    │ - Sessions      │
│ - Test reports  │    │ - Config data   │    │ - Temp data     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │      SRV        │ ← Plugin goes here!
                    │                 │
                    │ - Validation    │
                    │ - Plugins       │
                    │ - Business logic│
                    └─────────────────┘
```

**Important**: The plugin is deployed to the **`srv`** container, which handles all validation logic.

## 🔧 **Configuration Options**

### **Container Name**
- **Default**: `srv` (ITB service container)
- **Find yours**: `docker ps --format "{{.Names}}"`
- **Important**: Use the **service/backend container** name, not ui/mysql/redis
- **Example**: If your service container is named `itb-backend`, set `CONTAINER_NAME=itb-backend`

### **Domain Name**
- **Default**: `fhir-validation`
- **Custom**: Choose any name for your validation domain
- **Example**: `us-core-validation`, `european-fhir`, etc.

### **Domains Path**
Common ITB Docker paths (try these in order):

1. **`/opt/itb/domains`** (most common)
2. **`/var/lib/itb/domains`**
3. **`/home/itb/domains`**
4. **`/usr/local/itb/domains`**

**To find the correct path:**
```bash
# Check what's in your container
docker exec your-container-name ls -la /

# Look for directories like:
# /opt/itb/domains
# /var/lib/itb/domains
# /home/itb/domains
```

## 📋 **What the Script Does**

1. **Validates Environment**
   - Checks if Docker is running
   - Verifies container exists
   - Ensures plugin JAR is built

2. **Builds Plugin** (if needed)
   - Runs `mvn clean package`
   - Creates the shaded JAR

3. **Deploys to Container**
   - Creates `plugins` directory in domain
   - Copies plugin JAR to container
   - Configures domain settings

4. **Verifies Deployment**
   - Checks JAR is in place
   - Confirms configuration is correct

## 🐛 **Troubleshooting**

### **Container Not Found**
```bash
# Check container name
docker ps

# Use exact name from docker ps output
```

### **Permission Denied**
```bash
# Make script executable
chmod +x deploy-to-docker-itb.sh

# Or run with sudo (if needed)
sudo ./deploy-to-docker-itb.sh
```

### **Wrong Domains Path**
```bash
# Explore container structure
docker exec your-container-name find / -name "domains" -type d 2>/dev/null

# Update docker-deploy.config with correct path
```

### **Plugin Not Loading**
```bash
# Check container logs
docker logs your-container-name | grep -i plugin

# Restart container
docker restart your-container-name

# Or restart just ITB service
docker exec your-container-name supervisorctl restart itb
```

## 🔍 **Verification Commands**

### **Check Plugin Deployment**
```bash
# Verify JAR is in place
docker exec your-container-name ls -la /path/to/domains/your-domain/resources/plugins/

# Check configuration
docker exec your-container-name cat /path/to/domains/your-domain/config.properties
```

### **Test Plugin Loading**
```bash
# Check logs for plugin loading
docker logs your-container-name | grep -i "plugin\|fhir"

# Look for messages like:
# "Loaded 1 plugin(s) for domain|validationType"
# "Plugin loaded successfully"
```

## 📚 **Manual Deployment (Alternative)**

If the script doesn't work, you can deploy manually:

```bash
# 1. Build plugin
mvn clean package

# 2. Create directory structure
docker exec your-container mkdir -p /path/to/domains/your-domain/resources/plugins

# 3. Copy plugin JAR
docker cp target/json-fhir-validator-0.1.0-SNAPSHOT-shaded.jar your-container:/path/to/domains/your-domain/resources/plugins/

# 4. Create config.properties
docker exec your-container sh -c "cat > /path/to/domains/your-domain/config.properties << 'EOF'
domain.name = your-domain
domain.description = FHIR validation with plugin
validator.defaultPlugins.0.jar = resources/plugins/json-fhir-validator-0.1.0-SNAPSHOT-shaded.jar
validator.defaultPlugins.0.class = se.oskar.fhir.plugin.FhirJsonValidatorPlugin
EOF"

# 5. Restart container
docker restart your-container
```

## 🎯 **Next Steps After Deployment**

1. **Restart ITB service** to load the plugin:
   ```bash
   # Restart just the service container
   docker restart srv
   
   # Or if using docker-compose
   docker-compose restart srv
   ```

2. **Create test cases** that call the JSON validator
3. **Upload FHIR JSON files** for validation
4. **Plugin runs automatically** - no special test configuration needed

**Note**: Only restart the `srv` container - the ui, mysql, and redis containers can keep running.

## 🔗 **Related Documentation**

- [GITB Administrator Guide](GITB_ADMINISTRATOR_GUIDE.md)
- [ITB Compliance Report](ITB_COMPLIANCE_REPORT.md)
- [Main README](README.md)
