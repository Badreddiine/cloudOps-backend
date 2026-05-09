#!/bin/bash
set -e

NAMESPACE="keycloak"
RELEASE="keycloak"
CHART_VERSION="17.3.6"   # Bitnami Keycloak stable

echo "=== Création namespace ==="
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

echo "=== Application ConfigMap realm.json ==="
kubectl apply -f configmap-realm.yaml

echo "=== Application Secret admin ==="
kubectl apply -f secret-admin.yaml

echo "=== Ajout repo Bitnami ==="
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

echo "=== Déploiement Keycloak ==="
helm upgrade --install $RELEASE bitnami/keycloak \
  --namespace $NAMESPACE \
  --version $CHART_VERSION \
  --values values-prod.yaml \
  --wait \
  --timeout 10m

echo "=== Keycloak déployé ==="
kubectl get pods -n $NAMESPACE