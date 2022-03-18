package test;

import containerd.services.images.v1.ImagesGrpc;
import containerd.services.images.v1.ImagesOuterClass;
import containerd.services.images.v1.ImagesOuterClass.Image;
import containerd.services.images.v1.ImagesOuterClass.ListImagesResponse;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.EventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollDomainSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.unix.DomainSocketAddress;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
    @RequestMapping("test")
    public String test(@RequestParam(defaultValue = "default") String value) {
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

//        ImagesOuterClass.CreateImageRequest createImageRequest =
//            ImagesOuterClass.CreateImageRequest.newBuilder()
//                .mergeFrom(null) // 是否此处能够导入
//                    .build();
//
//        stub.create();
            // Make the RPC Call
            stub.list(request, new StreamObserver<ListImagesResponse>() {
                // When response is received iterate over the Response and print the names of images
                public void onNext(ImagesOuterClass.ListImagesResponse response) {
                    List<Image> images = response.getImagesList();
                    System.out.printf("images size: %s%n",images.size());
                    for (int i = 0; i < images.size(); i++) {
                        System.out.println(i + " -" + images.get(i).getName());
                    }
                }

                // if there is an error
                public void onError(Throwable t) {

                    System.out.printf("error la");
                    t.printStackTrace();
                }

                // when server completes the response and closes the stream shutdown our channel and EventLoopGroup
                public void onCompleted() {

                    System.out.printf("finished");
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
