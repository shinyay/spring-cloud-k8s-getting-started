#!/usr/bin/env fish

kubectl create configmap postgres-config \
    --from-literal=postgres.service.name=postgresql \
    --from-literal=postgres.db.name=employee

kubectl get configmap postgres-config -o yaml