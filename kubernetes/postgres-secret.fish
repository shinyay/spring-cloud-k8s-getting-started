#!/usr/bin/env fish

kubectl create secret generic db-security \
    --from-literal=db.user.name=sample \
    --from-literal=db.user.password=password
