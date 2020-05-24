# Spring Cloud Kubernetes Getting Started

Spring Cloud Kubernetes using Spring Data REST on kind

## Description

This project is deployed on [kind](https://kind.sigs.k8s.io).

### Spring Dependencies

The followings are dependencies for this project.

- spring-boot-starter-data-rest
- spring-boot-starter-data-jpa
- spring-cloud-starter-kubernetes-all
- postgresql

## Demo

### 1. Create kind cluster for Ingress

```shell script
$ kind create cluster --config=kubernetes/kind/cluster.yml
```

#### cluster.yml

```yaml
apiVersion: kind.x-k8s.io/v1alpha4
kind: Cluster
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
  extraPortMappings:
  - containerPort: 80
    hostPort: 80
    protocol: TCP
  - containerPort: 443
    hostPort: 443
    protocol: TCP
```

### 1.1. Deploy Ingress NGINX controller

```shell script
$ kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/static/provider/kind/deploy.yaml
```

Wait until is ready to process requests running:
```shell script
$ kubectl wait --namespace ingress-nginx \
    --for=condition=ready pod \
    --selector=app.kubernetes.io/component=controller \
    --timeout=90s
```

### 2. Deploy PostgreSQL

### 2.1. ConfigMap for PostgreSQL
```shell script
$ kubectl create configmap postgres-config \
      --from-literal=postgres.service.name=postgresql \
      --from-literal=postgres.db.name=employee
```

### 2.2. Secret for PostgreSQL
```shell script
$ kubectl create secret generic db-security \
    --from-literal=db.user.name=sample \
    --from-literal=db.user.password=password
```

### 2.3. Deployment for PostgreSQL
```shell script
$ kubectl apply -f kubernetes/postgres-deployment.yml
```

#### postgres-deployment.yml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgresql
  labels:
    app: postgresql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgresql
  template:
    metadata:
      labels:
        app: postgresql
    spec:
      volumes:
        - name: data
          emptyDir: {}
      containers:
        - name: postgres
          image: postgres:9.6.5
          env:
            - name: POSTGRES_USER
              valueFrom:
                secretKeyRef:
                  name: db-security
                  key: db.user.name
            - name: POSTGRES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-security
                  key: db.user.password
            - name: POSTGRES_DB
              valueFrom:
                configMapKeyRef:
                  name: postgres-config
                  key: postgres.db.name
          ports:
            - containerPort: 5432
          volumeMounts:
            - name: data
              mountPath: /var/lib/postgresql/
---
apiVersion: v1
kind: Service
metadata:
  name: postgresql
  labels:
    app: postgresql
spec:
  selector:
    app: postgresql
  ports:
    - port: 5432
```

### 3. Deploy Spring Configuration for ConfigMap
```shell script
$ kubectl create configmap app-config \
    --from-file=kubernetes/application.properties
```

#### application.properties
```properties
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=create
spring.datasource.url=jdbc:postgresql://${${POSTGRES_SERVICE}.service.host}:${${POSTGRES_SERVICE}.service.port}/${POSTGRES_DB_NAME}
spring.datasource.username=${POSTGRES_DB_USER}
spring.datasource.password=${POSTGRES_DB_PASSWORD}
```

### 4. Create Service Account for ConfifMap
```shell script
$ kubectl apply -f kubernetes/serviceaccount.yml
$ kubectl apply -f kubernetes/role.yml
$ kubectl apply -f kubernetes/rolebinding.yml
```

#### serviceaccount.yml
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: config-reader
  namespace: default

```

#### role.yml
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: pod-reader
  namespace: default
rules:
  - apiGroups: ["", "extensions", "apps"]
    resources: ["pods", "configmaps", "services", "endpoints", "secrets"]
    verbs: ["get", "watch", "list"]
```
#### rolebinding.yml
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: pod-reader
  namespace: default
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: pod-reader
subjects:
  - kind: ServiceAccount
    name: config-reader
    namespace: default
```

### 5. Build Spring container image

```shell script
$ ./gradlew clean jib
```

### 6. Deploy Spring Application
```shell script
$ kubectl apply -f kubernetes/deployment.yml
```

#### deployment.yml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app
  labels:
    app: spring-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: spring-app
  template:
    metadata:
      labels:
        app: spring-app
    spec:
      containers:
        - name: app
          image: shinyay/spring-cloud-k8s-gs:0.0.1-SNAPSHOT
          ports:
            - containerPort: 8080
          env:
            - name: POSTGRES_SERVICE
              valueFrom:
                configMapKeyRef:
                  name: postgres-config
                  key: postgres.service.name
            - name: POSTGRES_DB_NAME
              valueFrom:
                configMapKeyRef:
                  name: postgres-config
                  key: postgres.db.name
            - name: POSTGRES_DB_USER
              valueFrom:
                secretKeyRef:
                  name: db-security
                  key: db.user.name
            - name: POSTGRES_DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-security
                  key: db.user.password
      serviceAccountName: config-reader
---
apiVersion: v1
kind: Service
metadata:
  name: app
  labels:
    app: spring-app
spec:
  ports:
    - port: 8080
      protocol: TCP
  selector:
    app: spring-app
#  type: NodePort
---
# For KIND
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: example-ingress
spec:
  rules:
    - http:
        paths:
          - path: /
            backend:
              serviceName: app
              servicePort: 8080
```

### 7. Confirm Application Deployment
```shell script
$ curl -X POST http://localhost/employees \
      -H 'Content-Type: application/json' \
      -d '{
          "name": "JohnDoe",
          "email": "johndoe@demo.com"
      }'

# curl -X GET http://localhost/employees
```

## Features

- feature:1
- feature:2

## Requirement

## Usage

## Installation

## Licence

Released under the [MIT license](https://gist.githubusercontent.com/shinyay/56e54ee4c0e22db8211e05e70a63247e/raw/34c6fdd50d54aa8e23560c296424aeb61599aa71/LICENSE)

## Author

[shinyay](https://github.com/shinyay)
