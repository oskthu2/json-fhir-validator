# FHIR JSON Validator Plugin - Docker ITB Deployment Script (PowerShell)
# This script automates the deployment of the plugin to a Docker-based ITB instance

param(
    [string]$ContainerName = "itb-srv",
    [string]$DomainName = "fhir-validation",
    [string]$DomainsPath = "/opt/itb/domains"
)

# =============================================================================
# CONFIGURATION - MODIFY THESE VALUES FOR YOUR ENVIRONMENT
# =============================================================================

# Plugin information
$PluginJar = "target\json-fhir-validator-0.1.0-SNAPSHOT.jar"
$PluginName = "json-fhir-validator-0.1.0-SNAPSHOT.jar"

# =============================================================================
# VALIDATION
# =============================================================================

Write-Host "🔍 Validating environment..." -ForegroundColor Cyan

# Check if Docker is running
try {
    docker info | Out-Null
} catch {
    Write-Host "❌ Docker is not running or not accessible" -ForegroundColor Red
    exit 1
}

# Check if container exists
$containerExists = docker ps -q -f name="$ContainerName" | Select-String "."
if (-not $containerExists) {
    Write-Host "❌ Container '$ContainerName' not found or not running" -ForegroundColor Red
    Write-Host "Available containers:" -ForegroundColor Yellow
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    exit 1
}

# Check if plugin JAR exists
if (-not (Test-Path $PluginJar)) {
    Write-Host "❌ Plugin JAR not found: $PluginJar" -ForegroundColor Red
    Write-Host "Please run 'mvn clean package' first" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Environment validation passed" -ForegroundColor Green

# =============================================================================
# BUILD PLUGIN (if needed)
# =============================================================================

Write-Host "🔨 Building plugin..." -ForegroundColor Cyan
try {
    mvn clean package -q
    Write-Host "✅ Plugin built successfully" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to build plugin" -ForegroundColor Red
    exit 1
}

# =============================================================================
# DEPLOY TO DOCKER CONTAINER
# =============================================================================

Write-Host "🚀 Deploying plugin to ITB service container '$ContainerName'..." -ForegroundColor Cyan
Write-Host "📋 Note: ITB consists of 4 containers: srv (service), ui, mysql, redis" -ForegroundColor Yellow
Write-Host "🔌 Plugin will be deployed to the service container where validation happens" -ForegroundColor Yellow

# Create plugins directory in the domain
Write-Host "📁 Creating plugins directory..." -ForegroundColor Cyan
docker exec $ContainerName mkdir -p "$DomainsPath/$DomainName/resources/plugins"

# Copy plugin JAR to container
Write-Host "📦 Copying plugin JAR..." -ForegroundColor Cyan
docker cp $PluginJar "$ContainerName`:$DomainsPath/$DomainName/resources/plugins/"

# Create domain configuration if it doesn't exist
Write-Host "⚙️ Configuring domain..." -ForegroundColor Cyan
$DomainConfig = "$DomainsPath/$DomainName/config.properties"

# Check if domain config exists
$configExists = docker exec $ContainerName test -f $DomainConfig 2>$null
if ($configExists -eq 0) {
    Write-Host "📝 Domain config exists, updating plugin configuration..." -ForegroundColor Cyan
    
    # Remove existing plugin config if present
    docker exec $ContainerName sed -i '/validator\.defaultPlugins\.0\.jar/d' $DomainConfig
    docker exec $ContainerName sed -i '/validator\.defaultPlugins\.0\.class/d' $DomainConfig
    
    # Add plugin configuration
    docker exec $ContainerName sh -c "echo '' >> $DomainConfig"
    docker exec $ContainerName sh -c "echo '# FHIR JSON Validator Plugin' >> $DomainConfig"
    docker exec $ContainerName sh -c "echo 'validator.defaultPlugins.0.jar = resources/plugins/$PluginName' >> $DomainConfig"
    docker exec $ContainerName sh -c "echo 'validator.defaultPlugins.0.class = se.oskar.fhir.plugin.FhirJsonValidatorPlugin' >> $DomainConfig"
} else {
    Write-Host "📝 Creating new domain configuration..." -ForegroundColor Cyan
    docker exec $ContainerName sh -c "mkdir -p $DomainsPath/$DomainName"
    
    # Create basic domain config
    $configContent = @"
# FHIR Validation Domain Configuration
domain.name = $DomainName
domain.description = FHIR JSON validation with custom plugin

# Plugin configuration
validator.defaultPlugins.0.jar = resources/plugins/$PluginName
validator.defaultPlugins.0.class = se.oskar.fhir.plugin.FhirJsonValidatorPlugin
"@
    
    docker exec $ContainerName sh -c "cat > $DomainConfig << 'EOF'
$configContent
EOF"
}

# =============================================================================
# VERIFICATION
# =============================================================================

Write-Host "🔍 Verifying deployment..." -ForegroundColor Cyan

# Check if plugin JAR is in place
Write-Host "🔍 Checking if plugin JAR exists in container..." -ForegroundColor Cyan
$jarExists = docker exec $ContainerName ls -la "$DomainsPath/$DomainName/resources/plugins/$PluginName" 2>$null
if ($jarExists -and $jarExists -notmatch "No such file") {
    Write-Host "✅ Plugin JAR deployed successfully" -ForegroundColor Green
    Write-Host "📁 File details: $jarExists" -ForegroundColor Gray
} else {
    Write-Host "❌ Plugin JAR deployment failed" -ForegroundColor Red
    Write-Host "🔍 Debug: Checking container contents..." -ForegroundColor Yellow
    docker exec $ContainerName ls -la "$DomainsPath/$DomainName/resources/plugins/" 2>$null
    exit 1
}

# Check if config is correct
Write-Host "🔍 Checking if plugin configuration was added..." -ForegroundColor Cyan
$configCorrect = docker exec $ContainerName grep "validator.defaultPlugins.0.jar" $DomainConfig 2>$null
if ($configCorrect) {
    Write-Host "✅ Plugin configuration added successfully" -ForegroundColor Green
    Write-Host "📝 Config line: $configCorrect" -ForegroundColor Gray
} else {
    Write-Host "❌ Plugin configuration failed" -ForegroundColor Red
    Write-Host "🔍 Debug: Checking config file contents..." -ForegroundColor Yellow
    docker exec $ContainerName cat $DomainConfig 2>$null
    exit 1
}

# =============================================================================
# RESTART INSTRUCTIONS
# =============================================================================

Write-Host ""
Write-Host "🎉 Plugin deployment completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Next steps:" -ForegroundColor Yellow
Write-Host "1. Restart your ITB service to load the plugin:" -ForegroundColor White
Write-Host "   docker restart $ContainerName"
Write-Host ""
Write-Host "2. Or restart just the ITB service inside the container:" -ForegroundColor White
Write-Host "   docker exec $ContainerName supervisorctl restart itb"
Write-Host ""
Write-Host "3. Check the logs to verify plugin loading:" -ForegroundColor White
Write-Host "   docker logs $ContainerName | Select-String -Pattern 'plugin' -CaseSensitive:$false"
Write-Host ""
Write-Host "4. The plugin will now be available in domain: $DomainName" -ForegroundColor White
Write-Host "" -ForegroundColor White
Write-Host "5. If using docker-compose, you can restart just the service:" -ForegroundColor White
Write-Host "   docker-compose restart srv" -ForegroundColor White
Write-Host ""
Write-Host "🔗 For more information, see: GITB_ADMINISTRATOR_GUIDE.md" -ForegroundColor Cyan
