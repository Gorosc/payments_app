package org.cgoro;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.cgoro.model.PaymentOrder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class PaymentsAppTest extends MainApp{

    private static OkHttpClient http = new OkHttpClient();
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testHealth() throws IOException {

        Request request = new Request.Builder().url("http://localhost:8080/health").build();
        Response response = http.newCall(request).execute();
        assertEquals(200, response.code());
        assertEquals("{\"status\":\"UP\"}", response.body().string());
    }

   @Test
    public void submitPayment() throws IOException {
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setTransactionId("TXN"+ LocalDateTime.now().toString());
        paymentOrder.setAppRefId("APPPAYMENT"+Math.random());
        paymentOrder.setAmount(BigDecimal.valueOf(5000));
        paymentOrder.setRecipientAccountId(UUID.randomUUID().toString());
        paymentOrder.setSenderAccountId(UUID.randomUUID().toString());

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrder), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();

        Response response = http.newCall(request).execute();
        assertEquals(200, response.code());
        PaymentOrder responsePaymentOrder = objectMapper.readValue(response.body().string(), PaymentOrder.class);
        assertNotNull(responsePaymentOrder.getReceiptToken());
    }

    @Test
    public void submitPaymentDuplicateTransactionIdError() {
        throw new NotImplementedException();
    }

    @Test
    public void submitPaymentExistingSuccesfullAppRefIdError() {
        throw new NotImplementedException();
    }

    @Test
    public void submitPaymentInvalidSenderAccountError() {
        throw new NotImplementedException();
    }

    @Test
    public void submitPaymentInvalidRecipientAccountError() {
        throw new NotImplementedException();
    }

    @Test
    public void submitPaymentInsufficientFundsAccountError() {
        throw new NotImplementedException();
    }

    @Test
    public void submitPaymentNegativePaymentAmountError() {
        throw new NotImplementedException();
    }

    @Test
    public void submitPaymentEnquiry() {
        throw new NotImplementedException();
    }

    @Test
    public void submitPaymentRefundRequest() {
        throw new NotImplementedException();
    }

    @BeforeClass
    public static void startProcess() throws Exception {
        TestProcess.getInstance().startIfNotRunning();
    }

}
