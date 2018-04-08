package com.tlyong1024.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.tlyong.grpc.examples.demo.DemoProto;
import com.tlyong.grpc.examples.demo.DemoServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GrpcClient {
    private ManagedChannel channel;
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
    void init(String host, int port) throws SSLException, IOException {
  
//        SslContext sslContext = GrpcSslContexts.forClient().trustManager(TestUtils.loadCert("ca.pem")).build();
//        SslContext sslContext = GrpcSslContexts.forClient().trustManager().build();
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
        DemoProto.LoginRequest request = DemoProto.LoginRequest.newBuilder().setUserName("yyc").build();
        //同步阻塞调用  
        DemoProto.LoginResponse loginResponse = grpcClient.blockingStub.login(request);
        System.out.println("同步调用后返回"+loginResponse.getMsg());  
        //异步非阻塞调用  
        ListenableFuture<DemoProto.LoginResponse> loginResponse2 = grpcClient.futureStub.login(request);
        DemoProto.LoginResponse loginResponseFuture =  loginResponse2.get();
        System.out.println("异步调用后返回"+loginResponseFuture.getMsg());  
        grpcClient.shutdowm();  
    }  
      
}  