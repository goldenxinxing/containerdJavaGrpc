package com.test.dockerjava;

import cn.hutool.json.JSONUtil;
import cn.hutool.system.oshi.CpuInfo;
import cn.hutool.system.oshi.OshiUtil;
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

import java.io.*;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.examples.Expander;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

@Slf4j
@RestController
public class DockerJava {

    @Value("${test.param:default}")
    private String param;

    @Autowired
    private DockerClient dockerClient;

    @GetMapping("test/tar")
    public String testTar(String source, String targetDir) throws IOException, ArchiveException {
//        File targetDir = new File("/home/star_ubuntu/testtar");
//        String source = "/home/star_ubuntu/test.tar";
        Expander expander = new Expander();
        expander.expand(new TarFile(new File(source)), new File(targetDir));
        return "sucdess";
    }

    @GetMapping("springbootParam")
    public String springBootParam() {
        return param;
    }

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

    @GetMapping("systemCpuInfo")
    public void systemInfo() throws InterruptedException {
        for (int i = 10; i > 0; i--) {
            SystemInfo systemInfo = new SystemInfo();
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            long[] ticks = processor.getSystemCpuLoadTicks();
            long nice = ticks[CentralProcessor.TickType.NICE.getIndex()]
                - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
            long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
            long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
            long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()]
                - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
            long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()]
                - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
            long user = ticks[CentralProcessor.TickType.USER.getIndex()]
                - prevTicks[CentralProcessor.TickType.USER.getIndex()];
            long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()]
                - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
            long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()]
                - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
            long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
            log.info("idle总数= {}", idle);
            log.info("CPU总数 = {},CPU利用率 ={}", processor.getLogicalProcessorCount(),
                new DecimalFormat("#.##%").format(1.0 - (idle * 1.0 / totalCpu)));
            Thread.sleep(1000);
        }
    }

    @GetMapping("getHutoolCpuInfo")
    public CpuInfo getHutoolCpuInfo() {
        return OshiUtil.getCpuInfo();
    }

    public static void main(String[] args) {
        log.info("cpu info:{}", JSONUtil.toJsonStr(OshiUtil.getCpuInfo()));
    }
    /*public static void main(String[] args) {
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()]
            - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()]
            - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]
            - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()]
            - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()]
            - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()]
            - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()]
            - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()]
            - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        log.info("idle总数= {}", idle);
        log.info("CPU总数 = {},CPU利用率 ={}", processor.getLogicalProcessorCount(),
            new DecimalFormat("#.##%").format(1.0 - (idle * 1.0 / totalCpu)));
    }*/
    @GetMapping("systemMemInfo")
    public void systemMemInfo() throws InterruptedException {
        for (int i = 10; i > 0; i--) {
            SystemInfo systemInfo = new SystemInfo();
            GlobalMemory memory = systemInfo.getHardware().getMemory();
            long totalByte = memory.getTotal();
            long acaliableByte = memory.getAvailable();

            log.info("内存大小 = {},内存使用率 ={}", formatByte(totalByte),
                new DecimalFormat("#.##%").format((totalByte - acaliableByte) * 1.0 / totalByte));
            Thread.sleep(1000);
        }
    }

    public static String formatByte(long byteNumber) {
        double FORMAT = 1024.0;
        double kbNumber = byteNumber / FORMAT;
        if (kbNumber < FORMAT) {
            return new DecimalFormat("#.##KB").format(kbNumber);
        }
        double mbNumber = kbNumber / FORMAT;
        if (mbNumber < FORMAT) {
            return new DecimalFormat("#.##MB").format(mbNumber);
        }
        double gbNumber = mbNumber / FORMAT;
        if (gbNumber < FORMAT) {
            return new DecimalFormat("#.##GB").format(gbNumber);
        }
        double tbNumber = gbNumber / FORMAT;
        return new DecimalFormat("#.##TB").format(tbNumber);
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

        deviceRequest.withCapabilities(new ArrayList<List<String>>() {{
            add(new ArrayList<String>() {{
                add("gpu");
            }});
        }});
        deviceRequest.withDeviceIds(new ArrayList<String>() {{
            add(gpuId);
        }});

        Map<String, String> map = new HashMap<>();
        map.put("taskId", "123456");
        CreateContainerResponse response = dockerClient.createContainerCmd(image)
            .withExposedPorts(ExposedPort.tcp(port))
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withNetworkMode("host")
                    .withPortBindings(PortBinding.parse(String.format("%s:%s", port, port)))
                    .withDeviceRequests(new ArrayList<DeviceRequest>() {{
                        add(deviceRequest);
                    }})
            )
            .withLabels(map)
            .exec();
        log.info("container id:{}", response.getId());
        return response.getId();
    }

    @GetMapping("createContainerForNvidia")
    public String createContainerForNvidia(String image, String gpuId) {
        DeviceRequest deviceRequest = new DeviceRequest();

        deviceRequest.withCapabilities(new ArrayList<List<String>>() {{
            add(new ArrayList<String>() {{
                add("gpu");
            }});
        }});
        deviceRequest.withDeviceIds(new ArrayList<String>() {{
            add(gpuId);
        }});

        Map<String, String> map = new HashMap<>();
        map.put("taskId", "123456");
        CreateContainerResponse response = dockerClient.createContainerCmd(image)
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withNetworkMode("host")
                    .withDeviceRequests(new ArrayList<DeviceRequest>() {{
                        add(deviceRequest);
                    }})
            )
            .withCmd("nvidia-smi")
            .withLabels(map)
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
                if (object.getStatus().equals("stop")) {
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
