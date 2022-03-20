package test;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import containerd.services.containers.v1.ContainersGrpc;
import containerd.services.containers.v1.ContainersOuterClass;
import containerd.services.images.v1.ImagesGrpc;
import containerd.services.images.v1.ImagesOuterClass;
import containerd.services.images.v1.ImagesOuterClass.CreateImageRequest;
import containerd.services.images.v1.ImagesOuterClass.CreateImageResponse;
import containerd.services.images.v1.ImagesOuterClass.Image;
import containerd.services.images.v1.ImagesOuterClass.ListImagesResponse;
import containerd.services.tasks.v1.TasksGrpc;
import containerd.services.tasks.v1.TasksOuterClass;
import containerd.types.DescriptorOuterClass.Descriptor;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.EventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollDomainSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.unix.DomainSocketAddress;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}

@Slf4j
@RestController
class TestContainerd {

    private static final String namespace = "containerd-namespace";




    @GetMapping("container/run")
    public String runContainer(String containerId) {
        try {
            // Create a new channel using Netty Native transport
            EventLoopGroup elg = new EpollEventLoopGroup();
            ManagedChannel channel = NettyChannelBuilder
                .forAddress(
                    // todo 支持windows平台下能用
                    new DomainSocketAddress(
                        "/run/containerd/containerd.sock")).eventLoopGroup(elg)
                .channelType(EpollDomainSocketChannel.class)
                .usePlaintext()
                .build();

            // Since containerd requires a namespace to be specified when making a GRPC call, we will define a header with “containerd-namespace” key, set the value to our namespace
            Metadata header = new Metadata();
            Metadata.Key<String> key =
                Metadata.Key.of(namespace, Metadata.ASCII_STRING_MARSHALLER);
            header.put(key, "default");// "examplectr"
            //Create the stub and attach the header created above
            TasksGrpc.TasksStub stub = TasksGrpc.newStub(channel);
            stub = MetadataUtils.attachHeaders(stub, header);
            //Let’s build the ListImagesRequest with no filter

            TasksOuterClass.CreateTaskRequest request = TasksOuterClass.CreateTaskRequest.newBuilder()
                    .setContainerId(containerId)
                    .build();

            // Make the RPC Call
            stub.create(request, new StreamObserver<>() {

                @Override
                public void onNext(TasksOuterClass.CreateTaskResponse value) {
                    log.info(String.format("containerId: %s, id:%s", value.getContainerId(), value.getPid()));
                }

                @Override
                public void onError(Throwable t) {
                    log.error("run container error!");
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    log.info("run container finished");
                    channel.shutdownNow();
                    elg.shutdownGracefully(50, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            });

            return "success";
        } catch (Throwable e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }

    }
    @GetMapping("container/create")
    public String createContainer(String imageName, String containerId, String runtimeName) {
        try {
            // Create a new channel using Netty Native transport
            EventLoopGroup elg = new EpollEventLoopGroup();
            ManagedChannel channel = NettyChannelBuilder
                .forAddress(
                    // todo 支持windows平台下能用
                    new DomainSocketAddress(
                        "/run/containerd/containerd.sock")).eventLoopGroup(elg)
                .channelType(EpollDomainSocketChannel.class)
                .usePlaintext()
                .build();

            // Since containerd requires a namespace to be specified when making a GRPC call, we will define a header with “containerd-namespace” key, set the value to our namespace
            Metadata header = new Metadata();
            Metadata.Key<String> key =
                Metadata.Key.of(namespace, Metadata.ASCII_STRING_MARSHALLER);
            header.put(key, "default");// "examplectr"
            //Create the stub and attach the header created above
            ContainersGrpc.ContainersStub stub = ContainersGrpc.newStub(channel);
            stub = MetadataUtils.attachHeaders(stub, header);
            //Let’s build the ListImagesRequest with no filter

            ContainersOuterClass.CreateContainerRequest request =
                    ContainersOuterClass.CreateContainerRequest.newBuilder()
                            .setContainer(
                                    ContainersOuterClass.Container.newBuilder()
                                            .setImage(imageName)
                                            .setId(containerId)
                                            .setRuntime(ContainersOuterClass.Container.Runtime.newBuilder()
                                                    .setName(runtimeName)
                                                    .build())
                                            .setSpec(Any.newBuilder().build())
                                            .build()
                            )
                            .build();

            // Make the RPC Call
            stub.create(request, new StreamObserver<>() {
                @Override
                public void onNext(ContainersOuterClass.CreateContainerResponse value) {
                    log.info(String.format("container: %s, image:%s", value.getContainer(), value.getContainer().getImage()));
                }


                @Override
                public void onError(Throwable t) {
                    log.error("create container error!");
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    log.info("create container finished");
                    channel.shutdownNow();
                    elg.shutdownGracefully(50, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            });

            return "success";
        } catch (Throwable e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }

    }
    @GetMapping("image/create")
    public String createImage() {
        try {
            // Create a new channel using Netty Native transport
            EventLoopGroup elg = new EpollEventLoopGroup();
            ManagedChannel channel = NettyChannelBuilder
                    .forAddress(
                            // todo 支持windows平台下能用
                            new DomainSocketAddress(
                                    "/run/containerd/containerd.sock")).eventLoopGroup(elg)
                    .channelType(EpollDomainSocketChannel.class)
                    .usePlaintext()
                    .build();

            // Since containerd requires a namespace to be specified when making a GRPC call, we will define a header with “containerd-namespace” key, set the value to our namespace
            Metadata header = new Metadata();
            Metadata.Key<String> key =
                    Metadata.Key.of(namespace, Metadata.ASCII_STRING_MARSHALLER);
            header.put(key, "default");// "examplectr"
            //Create the stub and attach the header created above
            ImagesGrpc.ImagesStub stub = ImagesGrpc.newStub(channel);
            stub = MetadataUtils.attachHeaders(stub, header);
            //Let’s build the ListImagesRequest with no filter

            ImagesOuterClass.CreateImageRequest createImageRequest =
                    CreateImageRequest.newBuilder()
                            .setImage(Image.newBuilder()
                                    .setName("gxx-test")
                                    .setTarget(
                                            Descriptor.newBuilder().setDigest("123456").setSize(1000).build()
                                    ).build())
                            .build();

            // Make the RPC Call
            stub.create(createImageRequest, new StreamObserver<>() {
                @Override
                public void onNext(CreateImageResponse value) {
                    log.info(String.format("image: %s", value.getImage()));
                }

                @Override
                public void onError(Throwable t) {
                    log.error("list error!");
                    t.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    log.info("import finished");
                    channel.shutdownNow();
                    elg.shutdownGracefully(50, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            });

            return "success";
        } catch (Throwable e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }

    }
    @GetMapping("image/list")
    public String listImage(@RequestParam(defaultValue = "default") String value) {
        try {
            // Create a new channel using Netty Native transport
            EventLoopGroup elg = new EpollEventLoopGroup();
            ManagedChannel channel = NettyChannelBuilder
                .forAddress(
                    // todo 支持windows平台下能用
                    new DomainSocketAddress(
                        "/run/containerd/containerd.sock")).eventLoopGroup(elg)
                .channelType(EpollDomainSocketChannel.class)
                .usePlaintext()
                .build();
            // Since containerd requires a namespace to be specified when making a GRPC call, we will define a header with “containerd-namespace” key, set the value to our namespace
            Metadata header = new Metadata();
            Metadata.Key<String> key =
                Metadata.Key.of(namespace, Metadata.ASCII_STRING_MARSHALLER);
            header.put(key, value);// "examplectr"
            //Create the stub and attach the header created above
            ImagesGrpc.ImagesStub stub = ImagesGrpc.newStub(channel);
            stub = MetadataUtils.attachHeaders(stub, header);
            //Let’s build the ListImagesRequest with no filter
            ImagesOuterClass.ListImagesRequest request =
                ImagesOuterClass.ListImagesRequest.newBuilder()
                    .build();
            System.out.println(" ==============================================================");
            System.out.println("IMAGE\n == ==========");

            // Make the RPC Call
            stub.list(request, new StreamObserver<>() {
                // When response is received iterate over the Response and print the names of images
                public void onNext(ImagesOuterClass.ListImagesResponse response) {
                    List<Image> images = response.getImagesList();
                    log.info(String.format("list images size: %s", images.size()));
                    for (int i = 0; i < images.size(); i++) {
                        System.out.println(i + " -" + images.get(i).getName());
                    }
                }

                // if there is an error
                public void onError(Throwable t) {

                    log.error("list error!");
                    t.printStackTrace();
                }

                // when server completes the response and closes the stream shutdown our channel and EventLoopGroup
                public void onCompleted() {

                    log.info("list finished");
                    channel.shutdownNow();
                    elg.shutdownGracefully(50, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            });

            return "success";
        } catch (Throwable e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }

    }
}
