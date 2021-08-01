# k8s-logstash-fluentd-demo
Java multiline stacktrace logs parsing configurations for Kubernetes using: Logstash, Filebeat. Fluentd and Fluent-bit

To test configurations on your local machine you will need to have Kubernetes cluster. It will be preferable to have Docker Desktop with activated Kubernetes cluster.
In such case you don't need to push image of the `stacktrace_generator` service to Docker registry.

## Preparing required EK stack infrastructure
First we need to apply Kubernetes (k8s) configurations from `EK_stack` folder in next order:
- `namespace.yaml` - it will create required k8s namespace `logging` for our deployments.
- `elasticsearch.yaml` - it will deploy elasticsearch single node instance.
- `kibana.yaml` - it will deploy kibana connected to our elasticsearch instance. 

To apply k8s configuration to our cluster you need to execute next commands:
```shell
kubectl apply -f namespace.yaml
kubectl apply -f elasticsearch.yaml
kubectl apply -f kibana.yaml
```

We should have two pods in `Running` status. To check status of the pod deployments we can use next command:
```shell
$ kubectl -n logging get pods
NAME                             READY   STATUS    RESTARTS   AGE
elasticsearch-698987fdb4-q6s58   1/1     Running   0          24s
kibana-6f868bfc75-jhdsz          1/1     Running   0          16s
```

To check registered services for our pods we can use next commands:
```shell
$ kubectl -n logging get services
NAME            TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
elasticsearch   ClusterIP   None             <none>        9200/TCP   2m21s
kibana          ClusterIP   10.102.123.229   <none>        5601/TCP   2m14s
```

## Preparing Java stacktrace docker images
Next we need to build two images of the `stacktrace_generator` service by changing configuration in `stacktrace_generator/src/main/resources/logback-spring.xml`
log configuration file based on required version and build image with correspond version `1.0.0` or `2.0.0` in `stacktrace_generator/pom.xml` file.
To build image we need to have Maven, Java 11 and execute next commands from `stacktrace_generator` folder:
```shell
mvn clean package
mvn docker:build
```
We should have two docker images of the `stacktrace_generator` service with tags `1.0.0` (Console appender) and `2.0.0` (Logstash appender).

## Deployment of the log aggregators and shippers
To deploy log aggregator or log shipper navigate to the corresponding folder and apply k8s configurations in next order:
- `configMap.yaml` - it will create config map with configurations for log aggregator or shipper.
- `filebeat.yaml`, `fluentbit.yaml`, `fluentd.yaml`, `logstash.yaml` - it will deploy log aggregator or shipper using k8s DaemonSet configuration.
- `deployment.yaml` - it will deploy `stacktrace_generator` service of the required version `1.0.0` or `2.0.0`.

To apply k8s configuration to our cluster you need to execute next commands:
```shell
kubectl apply -f configMap.yaml
kubectl apply -f filebeat.yaml
kubectl apply -f deployment.yaml
```

When log aggregation configuration was applied, we can check status of our pods using next command:
```shell
kubectl -n logging get pods
```
We should have all pods in `Running` status.

To check logs in Kibana we need to port forward internal pod port to our local machine. 
To forward port replace `<kibana-pod-name>` placeholder with valid pod name from previous command and execute next command:
```shell
 kubectl -n logging port-forward <kibana-pod-name> 5601:5601
```
Now, in your web browser, visit the following URL:
```shell
http://localhost:5601
```

In Kibana UI we will need register index patters on `Management > Stack Management > Index Patterns` page. 
We will have two index patterns `logstash*` and `filebeat*` used by log aggregators and log shippers. 
After index patterns will be registered, we can check our logs on `Analytics > Discover` Kibana page.