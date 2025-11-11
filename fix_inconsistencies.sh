#!/bin/bash

# Script pour corriger automatiquement les incohérences restantes

set -e

echo "🔧 Correction des incohérences restantes..."

# 1. Renommer getAllEvidence -> getAllEvidences
echo "📝 Renommage getAllEvidence -> getAllEvidences..."
find ./app -name "*.kt" -type f -exec sed -i 's/getAllEvidence()/getAllEvidences()/g' {} \;
find ./app -name "*.kt" -type f -exec sed -i 's/getAllEvidenceSync()/getAllEvidencesSync()/g' {} \;
find ./app -name "*.kt" -type f -exec sed -i 's/fun getAllEvidence(/fun getAllEvidences(/g' {} \;
find ./app -name "*.kt" -type f -exec sed -i 's/suspend fun getAllEvidenceSync(/suspend fun getAllEvidencesSync(/g' {} \;

echo "✅ Corrections de nommage terminées"
echo "ℹ️  N'oubliez pas de commit ces changements"
