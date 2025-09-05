# Create ITB Test Suite ZIP Script
# This script creates a ZIP file of the test suite for easy import to ITB

Write-Host "📦 Creating ITB Test Suite ZIP file..." -ForegroundColor Cyan

# Get the current directory (should be itb-test-suite)
$currentDir = Get-Location
$zipFileName = "fhir-json-validation-test-suite.zip"

# Check if required files exist
$requiredFiles = @(
    "testSuite.xml",
    "testCases\validate-fhir-patient-basic.xml",
    "resources\patient-basic.json",
    "README.md"
)

foreach ($file in $requiredFiles) {
    if (-not (Test-Path $file)) {
        Write-Host "❌ Required file '$file' not found!" -ForegroundColor Red
        exit 1
    }
}

# Remove existing ZIP if it exists
if (Test-Path $zipFileName) {
    Remove-Item $zipFileName -Force
    Write-Host "🗑️ Removed existing ZIP file" -ForegroundColor Yellow
}

# Create the ZIP file
try {
    # Use .NET compression to create ZIP
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    
    # Get all files in the current directory (excluding the ZIP script itself)
    $files = Get-ChildItem -Path . -Recurse -File | Where-Object { $_.Name -notlike "*.ps1" -and $_.Name -notlike "*.sh" }
    
    # Create ZIP archive
    $compressionLevel = [System.IO.Compression.CompressionLevel]::Optimal
    $includeBaseDirectory = $false
    
    [System.IO.Compression.ZipFile]::CreateFromDirectory(
        ".", 
        $zipFileName, 
        $compressionLevel, 
        $includeBaseDirectory
    )
    
    Write-Host "✅ Test suite ZIP created successfully: $zipFileName" -ForegroundColor Green
    
    # Show ZIP contents
    Write-Host "📋 ZIP file contents:" -ForegroundColor Cyan
    $zip = [System.IO.Compression.ZipFile]::OpenRead($zipFileName)
    $zip.Entries | ForEach-Object { Write-Host "  - $($_.FullName)" -ForegroundColor Gray }
    $zip.Dispose()
    
    # Show file size
    $zipSize = (Get-Item $zipFileName).Length
    Write-Host "📏 ZIP file size: $([math]::Round($zipSize / 1KB, 2)) KB" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Failed to create ZIP file: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🎉 Ready to import to ITB!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Next steps:" -ForegroundColor Yellow
Write-Host "1. Go to your ITB instance → Test Suites → Import Test Suite" -ForegroundColor White
Write-Host "2. Upload the ZIP file: $zipFileName" -ForegroundColor White
Write-Host "3. Set domain to: fhir-validation" -ForegroundColor White
Write-Host "4. Import and run the test cases" -ForegroundColor White
Write-Host ""
Write-Host "🔗 For more information, see: README.md" -ForegroundColor Cyan
