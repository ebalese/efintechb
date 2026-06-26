param(
    [Parameter(Mandatory=$true)]
    [string]$SolutionName,
    
    [Parameter(Mandatory=$true)]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [string]$SolutionPath = "./artifacts",
    
    [Parameter(Mandatory=$false)]
    [switch]$Managed
)

# Load environment-specific settings
$settingsPath = "./config/$Environment/deployment-settings.json"
if (-not (Test-Path $settingsPath)) {
    Write-Error "Deployment settings not found for environment: $Environment"
    exit 1
}

$settings = Get-Content $settingsPath | ConvertFrom-Json

# Authenticate to Power Platform
Write-Host "Authenticating to Power Platform environment: $($settings.EnvironmentUrl)"
pac auth create --environment $settings.EnvironmentUrl

# Determine solution file
$solutionFile = if ($Managed) {
    "$SolutionPath/${SolutionName}_1.0.0.0_managed.zip"
} else {
    "$SolutionPath/${SolutionName}_unmanaged.zip"
}

if (-not (Test-Path $solutionFile)) {
    Write-Error "Solution file not found: $solutionFile"
    exit 1
}

# Import solution
Write-Host "Importing solution: $solutionFile"
pac solution import --path $solutionFile --async

# Publish solution
Write-Host "Publishing solution"
pac solution publish --async

Write-Host "Solution deployment completed successfully"
