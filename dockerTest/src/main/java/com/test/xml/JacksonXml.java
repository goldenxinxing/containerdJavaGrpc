package com.test.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JacksonXml {

    public static void main(String[] args) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        NvidiaSmiLog log = xmlMapper.readValue(
                new File("D:\\developSpace\\containerdJavaGrpc\\dockerTest\\src\\main\\resources\\nvidia-info-demo.xml"),
                NvidiaSmiLog.class);
        System.out.println(log.getCudaVersion());
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class NvidiaSmiLog {
    @JsonProperty("driver_version")
    private String driverVersion;
    @JsonProperty("cuda_version")
    private String cudaVersion;
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<GPU> gpu;
}
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class GPU {
    private String id;
    //名称
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("product_brand")
    private String productBrand;
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("fb_memory_usage")
    private FbMemoryUsage fbMemoryUsage;
    //进程信息
    //@JacksonXmlElementWrapper(useWrapping = false)
    private List<Process> processes;
}
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class FbMemoryUsage {
    //总内存
    private String total;
    //已用内存
    private String used;
    //空闲内存
    private String free;

}
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class Process {

    private String pid;
    @JsonProperty("process_name")
    private String processName;
    @JsonProperty("used_memory")
    private String usedMemory;

}