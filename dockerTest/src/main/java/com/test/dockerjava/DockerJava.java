package com.test.dockerjava;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import java.io.Closeable;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DockerJava {

    @GetMapping("test")
    public String test() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();

        DockerHttpClient.Request request = DockerHttpClient.Request.builder()
            .method(DockerHttpClient.Request.Method.GET)
            .path("/_ping")
            .build();

        try (DockerHttpClient.Response response = httpClient.execute(request)) {
            if (response.getStatusCode() == 200) {
                log.info("success");
            }
            if ("OK".equals(response.getBody())) {
                log.info("OK");
            }
        }

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        dockerClient.pingCmd().exec();
        return "success";
    }

    @GetMapping("createContainer")
    public String createContainer(String image, int port) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        CreateContainerResponse response = dockerClient.createContainerCmd(image)
            .withExposedPorts(ExposedPort.tcp(port))
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withNetworkMode("host")
                    .withPortBindings(PortBinding.parse(String.format("%s:%s",port, port)))
                    .withDevices()
            )
            .exec();
        log.info("container id:{}", response.getId());
        return response.getId();
    }

    @GetMapping("startContainer")
    public String startContainer(String containerId) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        log.info("start container id:{}", containerId);
        dockerClient.startContainerCmd(containerId).exec();
        return "success";
    }

    @GetMapping("statusOfContainer")
    public String statusOfContainer(String containerId) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        log.info("status of container id:{}", containerId);
        dockerClient.statsCmd(containerId).exec(new ResultCallback<Statistics>() {
            @Override
            public void onStart(Closeable closeable) {
                log.info("status start");
            }

            @Override
            public void onNext(Statistics object) {
                log.info("status of pid:{}",object.getPidsStats());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {
                log.info("status cmd complete");
            }

            @Override
            public void close() throws IOException {
                log.info("status cmd close");
            }
        });
        return "success";
    }
}
