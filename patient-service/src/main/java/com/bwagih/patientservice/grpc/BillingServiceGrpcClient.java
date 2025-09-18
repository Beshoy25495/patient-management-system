package com.bwagih.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import billing.BillingServiceGrpc.BillingServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class BillingServiceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    private BillingServiceBlockingStub blockingStub;

    @Value("${billing.service.address:localhost}") String serverAddress;
    @Value("${billing.service.grpc.port:9001}") int serverPort;


    @PostConstruct
    public void init() {
        log.info("Connecting to Billing Service GRPC service at {}:{}", serverAddress, serverPort);

        // create a channel to the billing service
        // usePlaintext() is used for development only, in production use SSL

        // http://localhost:9001/BillingService/CreateBillingAccount  --> this is in local environment
        // https://billing-service:9001/BillingService/CreateBillingAccount --> this is in production environment as a service
        // aws.grpc:billing-service:9001/BillingService/CreateBillingAccount --> this is in aws environment as a service
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).usePlaintext().build();

        // create a blocking stub to the billing service
        blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }




    public BillingResponse createBillingAccount(String patientId, String name, String email, double initialAmount) {

        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .setInitialAmount(initialAmount)
                .build();

        BillingResponse response = blockingStub.createBillingAccount(request);
        log.info("Received response from billing service via GRPC: {}", response);
        return response;
    }
}