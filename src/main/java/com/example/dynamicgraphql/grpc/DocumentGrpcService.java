package com.example.dynamicgraphql.grpc;

import com.example.dynamicgraphql.service.DocumentMutationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Empty;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DocumentGrpcService implements BindableService {

    private static final Logger log = LoggerFactory.getLogger(DocumentGrpcService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final DocumentMutationService mutationService;

    public DocumentGrpcService(DocumentMutationService mutationService) {
        this.mutationService = mutationService;
    }

    @Override
    public ServerServiceDefinition bindService() {
        return ServerServiceDefinition.builder("dynamic.DocumentService")
                .addMethod(io.grpc.ServerMethodDefinition.create(
                        io.grpc.MethodDescriptor.<Struct, Empty>newBuilder()
                                .setFullMethodName("dynamic.DocumentService/Create")
                                .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(Struct.getDefaultInstance()))
                                .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(Empty.getDefaultInstance()))
                                .build(),
                        new io.grpc.stub.ServerCalls.UnaryMethod<Struct, Empty>() {
                            @Override
                            public void invoke(Struct value, StreamObserver<Empty> observer) {
                                try {
                                    String json = JsonFormat.printer().omittingInsignificantWhitespace().print(value);
                                    Map<String, Object> document = OBJECT_MAPPER.readValue(json, MAP_TYPE);
                                    mutationService.persist(document);
                                    observer.onNext(Empty.getDefaultInstance());
                                    observer.onCompleted();
                                } catch (Exception e) {
                                    log.error("Failed to handle gRPC document payload", e);
                                    observer.onError(Status.INVALID_ARGUMENT
                                            .withDescription("Unable to decode document payload")
                                            .withCause(e)
                                            .asRuntimeException());
                                }
                            }
                        }))
                .build();
    }
}
