
# Hoymiles solar data provider

### Build project

```
mvn clean compile assembly:single
```

### Compile *.proto

```
./protoc --java_out=./src/main/java ./src/main/resources/proto/*.proto
```


### Enable experimental on docker-machine

1. Login into docker machine 

`docker-machine ssh`

2. Believe it or not, enabling the experimental features on the daemon side of things is actually quite simple. In order to do this, log into your server and issue the command:

`sudo nano /etc/docker/daemon.json`

This is a new file, so you won’t find anything contained within. Paste the following contents into this file:

```
{
"experimental": true
}
```

3. Restart docker-machine 

`docker-machine restart`

4. Register qemu 

`docker run --rm --privileged multiarch/qemu-user-static --reset -p yes`

5. Test

`docker run -t --rm --platform linux/arm/v7 alpine:latest uname -a`