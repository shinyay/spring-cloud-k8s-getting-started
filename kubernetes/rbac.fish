#!/usr/bin/env fish

kubectl apply -f serviceaccount.yml
kubectl apply -f role.yml
kubectl apply -f rolebinding.yml
