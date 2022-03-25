package com.test.gpu;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

@Slf4j
public class AnotherXmlWay {

    /**
     * 通过命令xml格式显卡信息
     *
     * @return xml字符串
     * @throws IOException 获取显卡信息错误
     */
    public static String getGpuXmlInfo() throws IOException {
        Process process;
        String result = "";
        process = Runtime.getRuntime().exec("nvidia-smi -q -x");
        try (InputStream inputStream = process.getInputStream()) {
            result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (process.isAlive()) {
            process.destroy();
        }
        return result;
    }

    private static final String REG = "<!DOCTYPE.*.dtd\">";

    /**
     * 获取gpu信息（暂时只支持nvidia-smi）
     *
     * @return gpu信息集合
     * @throws DocumentException xml解析错误
     */
    public static List<GPUInfo> convertXmlToGpuObject(String xmlGpu) throws DocumentException {
        //忽略dtd
        xmlGpu = xmlGpu.replaceAll(REG, "");
        Document document = DocumentHelper.parseText(xmlGpu);
        List<Element> gpu = document.getRootElement().elements("gpu");
        List<GPUInfo> gpuInfoList = new ArrayList<>();
        gpu.forEach(element -> {
            GPUInfo gpuInfo = new GPUInfo();
            String uuid = element.element("uuid").getText();
            Element fbMemoryUsage = element.element("fb_memory_usage");
            String total = fbMemoryUsage.element("total").getText();
            String used = fbMemoryUsage.element("used").getText();
            String free = fbMemoryUsage.element("free").getText();
            gpuInfo.setTotalMemory(total);
            gpuInfo.setUsedMemory(used);
            gpuInfo.setFreeMemory(free);
            gpuInfo.setName(uuid);
            Element processes = element.element("processes");
            List<Element> infos = processes.elements("process_info");
            List<ProcessInfo> processInfos = new ArrayList<>();
            infos.forEach(info -> {
                ProcessInfo processInfo = new ProcessInfo();
                String pid = info.element("pid").getText();
                String name = info.element("process_name").getText();
                String usedMemory = info.element("used_memory").getText();
                processInfo.setPid(pid);
                processInfo.setName(name);
                processInfo.setUsedMemory(usedMemory);
                processInfos.add(processInfo);
            });
            gpuInfo.setProcessInfos(processInfos);
            int intTotal = Integer.parseInt(total.split(" ")[0]);
            int intUsed = Integer.parseInt(used.split(" ")[0]);
            gpuInfo.setUsageRate((int) ((float) intUsed / intTotal * 100));
            gpuInfoList.add(gpuInfo);
        });
        return gpuInfoList;
    }

    /**
     * 获取gpu信息
     *
     * @return gpu信息集合
     */
    public static Optional<List<GPUInfo>> getGpuInfo() {
        try {
            String gpuXmlInfo = getGpuXmlInfo();
            List<GPUInfo> gpuInfos = convertXmlToGpuObject(gpuXmlInfo);
            return Optional.of(gpuInfos);
        } catch (Exception e) {
            log.error("获取gpu信息error , message : {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}

class GPUInfo {

    //名称
    private String name;
    //总内存
    private String totalMemory;
    //已用内存
    private String usedMemory;
    //空闲内存
    private String freeMemory;

    /**
     * 使用率 整形，最大为100
     */
    private int usageRate;
    //进程信息
    private List<ProcessInfo> processInfos;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(String totalMemory) {
        this.totalMemory = totalMemory;
    }

    public String getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(String usedMemory) {
        this.usedMemory = usedMemory;
    }

    public String getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(String freeMemory) {
        this.freeMemory = freeMemory;
    }

    public int getUsageRate() {
        return usageRate;
    }

    public void setUsageRate(int usageRate) {
        this.usageRate = usageRate;
    }

    public List<ProcessInfo> getProcessInfos() {
        return processInfos;
    }

    public void setProcessInfos(List<ProcessInfo> processInfos) {
        this.processInfos = processInfos;
    }
}

class ProcessInfo {

    private String pid;
    private String name;
    private String usedMemory;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(String usedMemory) {
        this.usedMemory = usedMemory;
    }
}
