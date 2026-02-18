# Script d'exécution des tests Bruno
# Phase 3 - Tests d'API & Validation Automatisée

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Tests API - Spring REST Microservice" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier si Bruno CLI est installé
Write-Host "Vérification de Bruno CLI..." -ForegroundColor Yellow
$bruInstalled = Get-Command bru -ErrorAction SilentlyContinue
if (-not $bruInstalled) {
    Write-Host "❌ Bruno CLI n'est pas installé!" -ForegroundColor Red
    Write-Host "Installation en cours..." -ForegroundColor Yellow
    npm install -g @usebruno/cli
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Échec de l'installation" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ Bruno CLI installé avec succès" -ForegroundColor Green
} else {
    Write-Host "✅ Bruno CLI détecté" -ForegroundColor Green
}

Write-Host ""

# Naviguer vers le dossier de tests
$testDir = "D:\spring-rest-med-amine-mamitiana-1\microservice\tests\bruno-collection"
if (-not (Test-Path $testDir)) {
    Write-Host "❌ Dossier de tests non trouvé: $testDir" -ForegroundColor Red
    exit 1
}

Set-Location $testDir
Write-Host "📁 Dossier de tests: $testDir" -ForegroundColor Cyan
Write-Host ""

# Menu
Write-Host "Choisissez une option:" -ForegroundColor Yellow
Write-Host "1. Lancer le scénario d'intégration (recommandé)" -ForegroundColor White
Write-Host "2. Lancer tous les tests" -ForegroundColor White
Write-Host "3. Lancer uniquement les tests d'erreur" -ForegroundColor White
Write-Host "4. Générer un rapport JSON" -ForegroundColor White
Write-Host "5. Générer un rapport HTML" -ForegroundColor White
Write-Host ""

$choice = Read-Host "Votre choix (1-5)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "🚀 Lancement du scénario d'intégration..." -ForegroundColor Cyan
        Write-Host ""
        bru run IntegrationScenario -r --env MicroService
    }
    "2" {
        Write-Host ""
        Write-Host "🚀 Lancement de tous les tests..." -ForegroundColor Cyan
        Write-Host ""
        bru run -r --env MicroService
    }
    "3" {
        Write-Host ""
        Write-Host "🚀 Lancement des tests d'erreur..." -ForegroundColor Cyan
        Write-Host ""
        bru run ErrorHandling -r --env MicroService
    }
    "4" {
        Write-Host ""
        Write-Host "🚀 Génération du rapport JSON..." -ForegroundColor Cyan
        Write-Host ""
        bru run -r --env MicroService --output rapport_execution.json
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✅ Rapport généré: rapport_execution.json" -ForegroundColor Green
        }
    }
    "5" {
        Write-Host ""
        Write-Host "🚀 Génération du rapport HTML..." -ForegroundColor Cyan
        Write-Host ""
        bru run -r --env MicroService --reporter-html rapport_execution.html
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✅ Rapport généré: rapport_execution.html" -ForegroundColor Green
        }
    }
    default {
        Write-Host "❌ Choix invalide" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Exécution terminée" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
