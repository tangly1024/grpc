## GRPC介绍
gRPC  是一个高性能、开源和通用的 gooogle开发RPC 框架，基于 HTTP/2 设计。目前提供 C、Java 和 Go 语言版本，分别是：grpc, grpc-java, grpc-go. 其中 C 版本支持 C, C++, Node.js, Python, Ruby, Objective-C, PHP 和 C# 支持.gRPC 基于 HTTP/2 标准设计，带来诸如双向流、流控、头部压缩、单 TCP 连接上的多复用请求等特。这些特性使得其在移动设备上表现更好，更省电和节省空间占用。

---
# 项目内容
-------------

> 以上的特性我们可以使用grpc取代http用与移动端和后台服务器之间的通信，还有服务与服务之间的通信。这样对于互联网公司来说，即减少了开发难度，又提高了效率。
  持续关注和分享：Android 性能调优、单元测试和重构、实用中间件、各种好玩的特效和黑科技、和小众刁钻没卵用需求的折腾记录。
 (如果你想打造个人品牌，把自己的介绍放在这里也是可以的)

#### 前提:
使用grpc,必须先了解protobuf，protobuf是谷歌自定义的序列化格式，类似与json，xml，只是json语义太弱，xml太笨重，而protobuf正结合了他们的优点。java使用protobuf的话必须要用maven插件，使之生成代码。

### 以下是一个简单的demo
- protobuf工程

- 实现grpc的客户端和服务端

### 注意:
该工程只有一个protobuf文件user.proto,这个文件必须要在src->main->proto目录下，否则打包不了。pom.xml配置如下:
``` xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.yyc.simple</groupId>
    <artifactId>protobuf</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>simple</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <grpc.version>0.13.2</grpc.version>
    </properties>

    <dependencies>
        <!-- <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-all</artifactId>
            <version>0.13.2</version>
        </dependency> -->
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-core</artifactId>
            <version>${grpc.version}</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-protobuf</artifactId>
            <version>${grpc.version}</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty</artifactId>
            <version>${grpc.version}</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-stub</artifactId>
            <version>${grpc.version}</version>
        </dependency>
        <dependency>
            <groupId>com.orbitz.consul</groupId>
            <artifactId>consul-client</artifactId>
            <version>0.10.0</version>
        </dependency>
    </dependencies>

    <build>
        <extensions>
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.4.1.Final</version>
            </extension>
        </extensions>
        <plugins>
            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.5.0</version>
                <configuration>
                    <!-- The version of protoc must match protobuf-java. If you don't depend
                        on protobuf-java directly, you will be transitively depending on the protobuf-java
                        version that grpc depends on. -->
                    <protocArtifact>com.google.protobuf:protoc:3.0.0-beta-2:exe:${os.detected.classifier}</protocArtifact>
                    <pluginId>grpc-java</pluginId>
                    <pluginArtifact>io.grpc:protoc-gen-grpc-java:0.13.2:exe:${os.detected.classifier}</pluginArtifact>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>compile-custom</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
### 还有一个工程grpc的客户端和服务端，pom.xml引用上面的工程 编写服务端实现如下:

``` java
import java.io.IOException;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.examples.demo.DemoProto.LoginRequest;
import io.grpc.examples.demo.DemoProto.LoginResponse;
import io.grpc.examples.demo.DemoServiceGrpc;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.ProtocolNegotiators;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.TestUtils;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

public class GrpcService {
    private int port=8888;
    private Server server;

    private void start() throws IOException{

        /**
         * 创建和启动一个服
         */
        server = NettyServerBuilder.forPort(port).addService(ServerInterceptors.intercept(DemoServiceGrpc.bindService(new Serviceimpl()),echoRequestHeadersInterceptor()))
                .build().start();
        //protocolNegotiator  和  sslContext  useTransportSecurity是等效的
        /**
         * 事件钩子  关闭的时候调用stop
         */
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
              public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GrpcService.this.stop();
                System.err.println("*** server shut down");
              }
        });

    }

    private void stop(){
        if(server!=null){
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException{
         if (server != null) {
             server.awaitTermination();
         }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final GrpcService grpcService = new GrpcService();
        grpcService.start();
        grpcService.blockUntilShutdown();
    }

    public static ServerInterceptor echoRequestHeadersInterceptor() {
        return new ServerInterceptor() {
            public <ReqT, RespT> Listener<ReqT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                    ServerCall<RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                System.out.println("服务器过滤器method"+method.toString()+"sss"+System.currentTimeMillis());
                System.out.println("服务器过滤器headers"+headers.toString());
                return next.startCall(method, call, headers);
            }

        };
      }

}

/**
 * 实现DemoService服务
 * @author yeyc
 *
 */
class  Serviceimpl implements   DemoServiceGrpc.DemoService{

    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        // TODO Auto-generated method stub

        System.out.println(request.getUserName()+"来登录");
        LoginResponse loginResponse = LoginResponse.newBuilder().setMsg(request.getUserName()+"授权登录成功").build();
        responseObserver.onNext(loginResponse);
        responseObserver.onCompleted();
    }
}

```

客户端实现:

``` java
package grpc;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.SimpleLoadBalancerFactory;
import io.grpc.TransportManager;
import io.grpc.examples.demo.DemoProto.LoginRequest;
import io.grpc.examples.demo.DemoProto.LoginResponse;
import io.grpc.examples.demo.DemoServiceGrpc;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.testing.TestUtils;
import io.netty.handler.ssl.SslContext;

public class GrpcClient {
    private  ManagedChannel channel;
    /**
     * 同步阻塞
     */
    private DemoServiceGrpc.DemoServiceBlockingStub blockingStub;
    /**
     * 异步非阻塞
     */
    private DemoServiceGrpc.DemoServiceFutureStub futureStub;

    /**
     * 创建channel  创建各种桩
     * @param host
     * @param port
     * @throws IOException
     * @throws SSLException
     */
    void init(String host, int port) throws SSLException, IOException{

        SslContext sslContext = GrpcSslContexts.forClient().trustManager(TestUtils.loadCert("ca.pem")).build();
        /*String  tagert = GrpcUtil.authorityFromHostAndPort(host, port);
        System.out.println("tagert:"+tagert);*/
        channel =  NettyChannelBuilder.forAddress(host, port).usePlaintext(true).build();
        //.negotiationType(NegotiationType.PLAINTEXT)  和这个是等效  usePlaintext(true)


        blockingStub = DemoServiceGrpc.newBlockingStub(channel);
        futureStub = DemoServiceGrpc.newFutureStub(channel);
    }



    public void shutdowm() throws InterruptedException{
        if(channel!=null){
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }



    public static void main(String[] args) throws InterruptedException, ExecutionException, SSLException, IOException {
        GrpcClient grpcClient = new GrpcClient();
        grpcClient.init("127.0.0.1", 8888);
        //构建请求参数
        LoginRequest request = LoginRequest.newBuilder().setUserName("yyc").build();
        //同步阻塞调用
        LoginResponse loginResponse = grpcClient.blockingStub.login(request);
        System.out.println("同步调用后返回"+loginResponse.getMsg());
        //异步非阻塞调用
        ListenableFuture<LoginResponse>  loginResponse2 = grpcClient.futureStub.login(request);
        LoginResponse loginResponseFuture =  loginResponse2.get();
        System.out.println("异步调用后返回"+loginResponseFuture.getMsg());
        grpcClient.shutdowm();
    }

}

```


### 使用方法

这样一个完整grpcdemo就可以使用了。

学习grpc的的网址:http://doc.oschina.net/grpc

下面是博主另外一篇博客阐述了grpc的应用场景：基于grpc的服务化框架

grpc-demo github 介绍了grpc的基本使用包括加密，客户端服务端流的使用，拦截器等的使用
