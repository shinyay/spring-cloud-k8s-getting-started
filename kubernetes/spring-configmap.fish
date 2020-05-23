#!/usr/bin/env fish

kubectl create configmap app-config \
    --from-file=application.properties
