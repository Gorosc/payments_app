package org.cgoro.rest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.cgoro.exception.DuplicateTransactionId;
import org.cgoro.model.PaymentOrder;

import java.util.UUID;

public class RestPaymentRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        restConfiguration().component("undertow").host("localhost").port(8080).bindingMode(RestBindingMode.json);

        onException(DuplicateTransactionId.class).handled(true)
                .process(exchange -> {
                    PaymentOrder paymentOrder = new PaymentOrder();
                    paymentOrder.setError(DuplicateTransactionId.class.getName());
                    paymentOrder.setMessage("Transaction ID already exists");
                    paymentOrder.setSuccess(true);
                    exchange.getIn().setBody(paymentOrder);
                });


        rest("/payment")
                .post().consumes("application/json").type(PaymentOrder.class).produces("application/json").outType(PaymentOrder.class)
                    .to("direct:validatePaymentOrder")
                    .to("direct:populateReceiptToken")
                .get("/health").produces("application/json").type(PaymentOrder.class)
                    .to("direct:health");


        from("direct:validatePaymentOrder")
                .to("direct:validateTransactionId");

        from("direct:validateTransactionId")
            .setHeader("payment_order",simple("${body}"))
            //.to("jpa:org.cgoro.db.Transaction?consumer.query=select o from org.cgoro.db.Transaction o where o.transactionId = ${body.transactionId}")
            .choice()
                .when(body().isNotNull()).throwException(new DuplicateTransactionId())
                .otherwise().setBody(header("payment_order"))
            .endChoice();


        from("direct:populateReceiptToken").process(exchange -> {
            PaymentOrder paymentOrder = exchange.getIn().getBody(PaymentOrder.class);
            paymentOrder.setReceiptToken(UUID.randomUUID().toString());
            exchange.getIn().setBody(paymentOrder);
        });

        from("direct:health").process(exchange -> {
            PaymentOrder paymentOrder = new PaymentOrder();
            paymentOrder.setSuccess(true);
            exchange.getIn().setBody(paymentOrder);
        });

    }
}
