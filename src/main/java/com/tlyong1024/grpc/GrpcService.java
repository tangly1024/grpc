package com.tlyong1024.grpc;

import com.tlyong.grpc.examples.demo.DemoProto;
import com.tlyong.grpc.examples.demo.DemoServiceGrpc;
import io.grpc.*;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class GrpcService {
    private int port = 8888;
    private Server server;

    private void start() throws IOException {
        System.out.println("启动服务");
        /**
         * 创建和启动一个服
         */
        server = NettyServerBuilder.forPort(port).addService(ServerInterceptors.intercept(DemoServiceGrpc.bindService(new Serviceimpl()), echoRequestHeadersInterceptor()))
                .build().start();
        //protocolNegotiator  和  sslContext  useTransportSecurity是等效的
        /**
         * 事件钩子  关闭的时候调用stop
         */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GrpcService.this.stop();
                System.err.println("*** server shut down");
            }
        });

    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
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
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                                         ServerCall<RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                System.out.println("服务器过滤器method" + method.toString() + "sss" + System.currentTimeMillis());
                System.out.println("服务器过滤器headers" + headers.toString());
                return next.startCall(method, call, headers);
            }

        };
    }

}

/**
 * 实现DemoService服务
 *
 * @author yeyc
 */
class Serviceimpl implements DemoServiceGrpc.DemoService {

    @Override
    public void login(DemoProto.LoginRequest request, StreamObserver<DemoProto.LoginResponse> responseObserver) {
        System.out.println(request.getUserName() + "来登录");
        DemoProto.LoginResponse loginResponse = DemoProto.LoginResponse.newBuilder().setMsg(request.getUserName() + "授权登录成功").build();
        responseObserver.onNext(loginResponse);
        responseObserver.onCompleted();
    }

}