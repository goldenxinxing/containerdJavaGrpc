package com.test.dockerjava;

import cn.hutool.json.JSONUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Device;
import com.github.dockerjava.api.model.DeviceRequest;
import com.github.dockerjava.api.model.Event;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DockerJava {

    @Autowired
    private DockerClient dockerClient;

    @GetMapping("jvmUsage")
    public String usage() {
        /* Total number of processors or cores available to the JVM */
        System.out.println("Available processors (cores): " +
            Runtime.getRuntime().availableProcessors());

        /* Total amount of free memory available to the JVM */
        System.out.println("Free memory (bytes): " +
            Runtime.getRuntime().freeMemory());

        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        /* Maximum amount of memory the JVM will attempt to use */
        System.out.println("Maximum memory (bytes): " +
            (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));

        /* Total memory currently in use by the JVM */
        System.out.println("Total memory (bytes): " +
            Runtime.getRuntime().totalMemory());

        /* Get a list of all filesystem roots on this system */
        File[] roots = File.listRoots();

        /* For each filesystem root, print some info */
        for (File root : roots) {
            System.out.println("File system root: " + root.getAbsolutePath());
            System.out.println("Total space (bytes): " + root.getTotalSpace());
            System.out.println("Free space (bytes): " + root.getFreeSpace());
            System.out.println("Usable space (bytes): " + root.getUsableSpace());
        }
        return "success";
    }

    @GetMapping("info")
    public Info info() {
        return dockerClient.infoCmd().exec();
    }

    @GetMapping("listImage")
    public List<Image> listImage() {
        List<Image> imageList = dockerClient.listImagesCmd().exec();
        for (Image image : imageList) {
            log.info("imageId:{}", image.getId());
        }
        return imageList;
    }

    @GetMapping("listContainer")
    public List<Container> listContainer() {
        return dockerClient.listContainersCmd().withShowAll(true).exec();
    }

    @GetMapping("createContainer")
    public String createContainer(String image, int port, String gpuId) {
        DeviceRequest deviceRequest = new DeviceRequest();

        deviceRequest.withCapabilities(new ArrayList<>(){{add(new ArrayList<>(){{add("gpu");}});}});
        deviceRequest.withDeviceIds(new ArrayList<>(){{add(gpuId);}});
        CreateContainerResponse response = dockerClient.createContainerCmd(image)
            .withExposedPorts(ExposedPort.tcp(port))
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withNetworkMode("host")
                    .withPortBindings(PortBinding.parse(String.format("%s:%s", port, port)))
                    .withDeviceRequests(new ArrayList<>(){{add(deviceRequest);}})
            )
            .withLabels(Map.of("taskId", "123456"))
            .exec();
        log.info("container id:{}", response.getId());
        return response.getId();
    }

    @GetMapping("startContainer")
    public String startContainer(String containerId) {

        log.info("start container id:{}", containerId);
        dockerClient.startContainerCmd(containerId).exec();
        return "success";
    }

    @GetMapping("statusOfContainer")
    public String statusOfContainer(String containerId) {
        log.info("status of container id:{}", containerId);
        dockerClient.statsCmd(containerId).exec(new ResultCallback<Statistics>() {
            @Override
            public void onStart(Closeable closeable) {
                log.info("status start");
            }

            @Override
            public void onNext(Statistics statistics) {
                log.info("status of id:{}, info:{}", containerId, JSONUtil.toJsonStr(statistics));
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

    @GetMapping("inspectOfContainer")
    public String inspectOfContainer(String containerId) {
        try {
            log.info("status of container id:{}", containerId);
            InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId)
                .exec();
            if (Boolean.TRUE.equals(response.getState().getRunning())) {
                return JSONUtil.toJsonStr(response);
            } else {
                return "";
            }

        } catch (NotFoundException e) {
            return "无此container";
        }

    }
    @GetMapping("events")
    public String events(String containerId) {
        ResultCallback<Event> resultCallback = new ResultCallback<Event>() {
            @Override
            public void onStart(Closeable closeable) {
                log.info("events start");
            }

            @Override
            public void onNext(Event object) {
                log.info("onNext:{}", JSONUtil.toJsonStr(object));
                if(object.getStatus().equals("stop")) {
                    log.info("invoke close");
                    throw new RuntimeException("time to stop");
                }

            }

            @Override
            public void onError(Throwable throwable) {
                log.error("events error");
            }

            @Override
            public void onComplete() {
                log.info("events complete");
            }

            @Override
            public void close() throws IOException {
                log.error("events close");
            }
        };
        dockerClient.eventsCmd().withContainerFilter(containerId).exec(resultCallback);
        return "success";
    }
}
