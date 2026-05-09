#!/bin/bash
set -e

NAMESPACE="monitoring"

echo "=== Création namespace monitoring ==="
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

echo "=== Ajout repos Helm ==="
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add grafana https://grafana.github.io/helm-charts
helm repo add jaegertracing https://jaegertracing.github.io/helm-charts
helm repo update

echo "=== Installation Prometheus ==="
helm upgrade --install prometheus prometheus-community/kube-prometheus-stack \
  --namespace $NAMESPACE \
  --values prometheus-values.yaml \
  --wait --timeout 10m

echo "=== Installation Grafana ==="
helm upgrade --install grafana grafana/grafana \
  --namespace $NAMESPACE \
  --values grafana-values.yaml \
  --wait --timeout 5m

echo "=== Application alertes Grafana ==="
kubectl apply -f grafana-alerts.yaml

echo "=== Installation Jaeger ==="
helm upgrade --install jaeger jaegertracing/jaeger \
  --namespace $NAMESPACE \
  --values jaeger-values.yaml \
  --wait --timeout 5m

echo "=== Application config OpenTelemetry ==="
kubectl apply -f otel-configmap.yaml

echo "=== Installation Loki ==="
helm upgrade --install loki grafana/loki \
  --namespace $NAMESPACE \
  --values loki-values.yaml \
  --wait --timeout 5m

echo "=== Installation Promtail ==="
helm upgrade --install promtail grafana/promtail \
  --namespace $NAMESPACE \
  --values promtail-values.yaml \
  --wait --timeout 5m

echo ""
echo "=== Monitoring déployé ==="
kubectl get pods -n $NAMESPACE
echo ""
echo "Grafana  : https://grafana.cloudops.io"
echo "Jaeger   : https://jaeger.cloudops.io"