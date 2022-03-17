
# Hoymiles solar data provider

### Build project

```
mvn clean compile assembly:single
```

### Compile *.proto

```
./protoc --java_out=./src/main/java ./src/main/resources/proto/*.proto
```