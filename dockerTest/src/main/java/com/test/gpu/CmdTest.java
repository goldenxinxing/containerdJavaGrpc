package com.test.gpu;

import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CmdTest {

    @GetMapping("cmdGpu")
    public List<GPUInfo> test() {
        Optional<List<GPUInfo>> gpu = AnotherXmlWay.getGpuInfo();
        return gpu.orElse(null);
    }
}
