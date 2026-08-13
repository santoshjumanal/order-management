package com.fulfilment.application.monolith.fulfillment;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("fulfillment")
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentResource {

    @Inject FulfillmentService fulfillmentService;

    @Inject FulfillmentRepository fulfillmentRepository;

    public static class FulfillmentRequest {
        public Long productId;
        public Long storeId;
        public String warehouseBusinessUnitCode;
    }

    public static class FulfillmentResponse {
        public Long id;
        public Long productId;
        public Long storeId;
        public String warehouseBusinessUnitCode;

        public FulfillmentResponse(FulfillmentUnit unit) {
            this.id = unit.id;
            this.productId = unit.product.id;
            this.storeId = unit.store.id;
            this.warehouseBusinessUnitCode = unit.warehouseBusinessUnitCode;
        }
    }

    @GET
    public List<FulfillmentResponse> listAll() {
        return fulfillmentRepository.listAll().stream().map(FulfillmentResponse::new).toList();
    }

    @GET
    @Path("store/{storeId}")
    public List<FulfillmentResponse> listByStore(@PathParam("storeId") Long storeId) {
        return fulfillmentRepository.listByStore(storeId).stream()
                .map(FulfillmentResponse::new)
                .toList();
    }

    @POST
    public Response associate(FulfillmentRequest request) {
        FulfillmentUnit unit =
                fulfillmentService.associate(
                        request.productId, request.storeId, request.warehouseBusinessUnitCode);
        return Response.ok(new FulfillmentResponse(unit)).status(201).build();
    }
}
