#!/bin/bash

# Create ITB Test Suite ZIP Script
# This script creates a ZIP file of the test suite for easy import to ITB

echo "📦 Creating ITB Test Suite ZIP file..."

# Get the current directory (should be itb-test-suite)
CURRENT_DIR=$(pwd)
TEST_SUITE_DIR="fhir-validation"
ZIP_FILE_NAME="fhir-validation-test-suite.zip"

# Check if the test suite directory exists
if [ ! -d "$TEST_SUITE_DIR" ]; then
    echo "❌ Test suite directory '$TEST_SUITE_DIR' not found!"
    echo "Please run this script from the itb-test-suite directory"
    exit 1
fi

# Check if required files exist
REQUIRED_FILES=(
    "$TEST_SUITE_DIR/test-suite.xml"
    "$TEST_SUITE_DIR/resources/patient.json"
    "$TEST_SUITE_DIR/README.md"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        echo "❌ Required file '$file' not found!"
        exit 1
    fi
done

# Remove existing ZIP if it exists
if [ -f "$ZIP_FILE_NAME" ]; then
    rm "$ZIP_FILE_NAME"
    echo "🗑️ Removed existing ZIP file"
fi

# Create the ZIP file
echo "🔨 Creating ZIP archive..."
if zip -r "$ZIP_FILE_NAME" "$TEST_SUITE_DIR" -x "*.DS_Store" "*/.*"; then
    echo "✅ Test suite ZIP created successfully: $ZIP_FILE_NAME"
    
    # Show ZIP contents
    echo "📋 ZIP file contents:"
    unzip -l "$ZIP_FILE_NAME" | grep -E "^[[:space:]]*[0-9]+" | while read -r line; do
        if [[ $line =~ [0-9]+[[:space:]]+[0-9]+[[:space:]]+[0-9]+[[:space:]]+(.+)$ ]]; then
            echo "  - ${BASH_REMATCH[1]}"
        fi
    done
    
    # Show file size
    ZIP_SIZE=$(stat -f%z "$ZIP_FILE_NAME" 2>/dev/null || stat -c%s "$ZIP_FILE_NAME" 2>/dev/null || echo "unknown")
    if [ "$ZIP_SIZE" != "unknown" ]; then
        ZIP_SIZE_KB=$(echo "scale=2; $ZIP_SIZE / 1024" | bc 2>/dev/null || echo "unknown")
        echo "📏 ZIP file size: ${ZIP_SIZE_KB} KB"
    fi
    
else
    echo "❌ Failed to create ZIP file"
    exit 1
fi

echo ""
echo "🎉 Ready to import to ITB!"
echo ""
echo "📋 Next steps:"
echo "1. Go to your ITB instance → Test Suites → Import Test Suite"
echo "2. Upload the ZIP file: $ZIP_FILE_NAME"
echo "3. Set domain to: fhir-validation"
echo "4. Import and run the test cases"
echo ""
echo "🔗 For more information, see: $TEST_SUITE_DIR/README.md"

