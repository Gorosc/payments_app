package org.cgoro.rest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.cgoro.db.entity.Account;
import org.cgoro.model.AccountDTO;
import org.cgoro.model.PaymentOrderDTO;
import org.cgoro.model.ReceiptDTO;

import java.util.HashMap;
import java.util.Map;

public class RestRouteBuilder extends RouteBuilder {

    public static final String APPLICATION_JSON = "application/json";

    @Override
    public void configure() throws Exception {

        restConfiguration().component("undertow").host("localhost").port(8080).bindingMode(RestBindingMode.json);

        rest("/payment")
                .post().consumes(APPLICATION_JSON).type(PaymentOrderDTO.class).produces(APPLICATION_JSON).outType(PaymentOrderDTO.class)
                    .to("direct:submitPayment")
                .get("/finalize").produces(APPLICATION_JSON).outType(ReceiptDTO.class)
                    .to("direct:finalizePayment")
                .get("/enquire").produces(APPLICATION_JSON).outType(ReceiptDTO.class)
                    .to("direct:enquirePaymentStatus");

        rest("/account")
                .get().produces(APPLICATION_JSON).outType(AccountDTO.class).to("direct:getAccounts")
                .get("/{id}").produces(APPLICATION_JSON).outType(AccountDTO.class).to("direct:getAccount");

        rest("/health").get().produces(APPLICATION_JSON).type(PaymentOrderDTO.class)
                .to("direct:health");

        from("direct:health").process(exchange -> {
            Map<String,String> status = new HashMap<>();
            status.put("status", "UP");
            exchange.getIn().setBody(status);
        });
    }
}
