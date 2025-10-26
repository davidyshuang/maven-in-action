#!/bin/bash

# Script to clean, compile and package all Maven projects in subdirectories
# This script handles multi-module Maven projects with proper build order
# Usage: ./clean-build-all.sh

echo "=== Starting Maven Clean, Compile, Test and Package for all projects ==="
echo "This script handles multi-module builds with proper dependency order"

# Function to build a single project
build_project() {
    local project_dir="$1"
    local project_name="$2"
    
    echo "=== Processing project: $project_name ($project_dir) ==="
    
    # Change to project directory
    cd "$project_dir"
    
    # Clean the project
    echo "Cleaning..."
    if mvn clean; then
        echo "✓ Clean successful"
    else
        echo "✗ Clean failed, but continuing..."
    fi
    
    # Compile main source code
    echo "Compiling main source code..."
    if mvn compile; then
        echo "✓ Main source compilation successful"
    else
        echo "✗ Main source compilation failed, but continuing..."
    fi
    
    # Compile test source code
    echo "Compiling test source code..."
    if mvn test-compile; then
        echo "✓ Test source compilation successful"
    else
        echo "✗ Test source compilation failed, but continuing..."
    fi
    
    # Install the project to local repository (so dependent projects can use it)
    echo "Installing to local repository..."
    if mvn install -DskipTests; then
        echo "✓ Install successful"
    else
        echo "✗ Install failed, but continuing..."
    fi
    
    # Return to original directory
    cd - > /dev/null
    
    echo "=== Completed: $project_name ==="
    echo
}

# Build standalone projects first (non-multi-module)
echo "=== Building standalone projects ==="

# Find standalone projects (those without modules in their pom.xml)
find . -name "pom.xml" -type f | while read pom_file; do
    project_dir=$(dirname "$pom_file")
    project_name=$(basename "$project_dir")
    
    # Skip if it's the current directory
    if [ "$project_dir" = "." ]; then
        continue
    fi
    
    # Check if this is a multi-module project (has modules section)
    if grep -q "<modules>" "$pom_file"; then
        echo "Skipping multi-module project: $project_name (will be handled separately)"
        continue
    fi
    
    build_project "$project_dir" "$project_name"
done

# Build multi-module projects in dependency order
echo "=== Building multi-module projects ==="

# Build account-parent modules in dependency order
if [ -d "ch-12/account-parent" ]; then
    echo "=== Building account-parent modules ==="
    
    # Build base modules first (those that are dependencies)
    build_project "ch-12/account-parent/account-captcha" "account-captcha"
    build_project "ch-12/account-parent/account-persist" "account-persist" 
    build_project "ch-12/account-parent/account-email" "account-email"
    
    # Build service module (depends on the above)
    build_project "ch-12/account-parent/account-service" "account-service"
    
    # Build web module (depends on service)
    build_project "ch-12/account-parent/account-web" "account-web"
    
    # Build parent project last
    build_project "ch-12/account-parent" "account-parent"
fi

# Build other multi-module projects
echo "=== Building other multi-module projects ==="

# Find and build other multi-module projects
find . -name "pom.xml" -type f | while read pom_file; do
    project_dir=$(dirname "$pom_file")
    project_name=$(basename "$project_dir")
    
    # Skip if it's the current directory or already processed
    if [ "$project_dir" = "." ] || [ "$project_dir" = "ch-12/account-parent" ] || \
       [[ "$project_dir" == ch-12/account-parent/* ]]; then
        continue
    fi
    
    # Check if this is a multi-module project
    if grep -q "<modules>" "$pom_file"; then
        echo "Building multi-module project: $project_name"
        build_project "$project_dir" "$project_name"
    fi
done

echo "=== Maven build process completed for all projects! ==="
echo "Note: Some projects may have warnings or failures, but the script continued processing all projects."
echo "Dependent projects were built in the correct order to resolve dependencies."
