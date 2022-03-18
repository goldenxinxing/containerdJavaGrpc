package test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import java.net.URI;
import java.net.URISyntaxException;

public class TestDocker {

    public static void main(String[] args) throws URISyntaxException {
        DockerClient client = DockerClientImpl.getInstance(
            DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost("npipe://localhost").build(),
            new ApacheDockerHttpClient.Builder().dockerHost(new URI("npipe://localhost:2375")).build());
        ListImagesCmd listImagesCmd= client.listImagesCmd();//.getFilters().forEach((s, strings) -> System.out.println(s));
        listImagesCmd.withShowAll(true);
    }
}
