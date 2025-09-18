package com.bwagih.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createBillingAccount(BillingRequest billingRequest,
                                     StreamObserver<BillingResponse> responseObserver) {

        log.info("createBillingAccount request received {}", billingRequest.toString());

        //TODO: implement the Business logic :-
        // 1. Validate Request (patient_id is not null and valid , and initial_amount is not null and valid)
        // 2. check if the patient has an active billing account :- Call External Service
        // 3. if the patient has an active billing account return error
        // 4. if the patient has an inactive billing account or other status return error
        // 5. if the patient does not have an active billing account create a new billing account :- save the billing account in the billing-service-db database
        // 6.  Send Notifications :- Call External Services (Notification Service) to Send Notifications to the patient
        // 7. return the response


        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("ACTIVE")
                .build();

        // send the response back to the client
        // (in grpc we use onNext to send the response
        // and onCompleted to complete the response,
        // and we can use onError to send an error)
        // in grpc we can send multiple responses to the client using onNext

        responseObserver.onNext(response);
        // complete the response
        responseObserver.onCompleted();
    }
}