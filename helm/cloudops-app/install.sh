#!/bin/bash
set -e

ENV=${1:-dev}
NAMESPACE="cloudops"
RELEASE="cloudops-app"

echo "=== Déploiement environnement : $ENV ==="

# Créer namespace
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Installer cert-manager si absent
if ! helm status cert-manager -n cert-manager &>/dev/null; then
  echo "=== Installation cert-manager ==="
  helm repo add jetstack https://charts.jetstack.io
  helm repo update
  helm upgrade --install cert-manager jetstack/cert-manager \
    --namespace cert-manager --create-namespace \
    --set installCRDs=true \
    --wait
fi

# Installer Ingress NGINX si absent
if ! helm status ingress-nginx -n ingress-nginx &>/dev/null; then
  echo "=== Installation Ingress NGINX ==="
  helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
  helm repo update
  helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
    --namespace ingress-nginx --create-namespace \
    --set controller.service.type=LoadBalancer \
    --wait
fi

# Déployer l'application
echo "=== Déploiement microservices ($ENV) ==="
helm upgrade --install $RELEASE . \
  --namespace $NAMESPACE \
  --values values.yaml \
  --values values-$ENV.yaml \
  --wait \
  --timeout 10m

echo "=== Déploiement terminé ==="
kubectl get pods -n $NAMESPACE
kubectl get ingress -n $NAMESPACE