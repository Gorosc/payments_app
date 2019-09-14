package org.cgoro;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.cgoro.db.entity.LedgerUpdateStatus;
import org.cgoro.db.entity.Transaction;
import org.cgoro.db.entity.TransactionStatus;
import org.cgoro.domain.LedgerRouteBuilder;
import org.cgoro.model.PaymentOrderDTO;
import org.cgoro.model.ReceiptDTO;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        String transactionId = "TXN"+ LocalDateTime.now().toString();
        paymentOrderDTO.setTransactionId(transactionId);
        paymentOrderDTO.setApplicationRefId("APPPAYMENT"+Math.random());
        paymentOrderDTO.setAmount(BigDecimal.valueOf(5000));
        paymentOrderDTO.setRecipientAccountId("ACCOUNT2");
        paymentOrderDTO.setSenderAccountId("ACCOUNT1");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();

        Response response = http.newCall(request).execute();
        assertEquals(200, response.code());
        PaymentOrderDTO responsePaymentOrderDTO = objectMapper.readValue(response.body().string(), PaymentOrderDTO.class);
        assertNotNull(responsePaymentOrderDTO.getReceiptToken());

        Transaction transaction = di.transactionDAO().find(transactionId);
        assertEquals(paymentOrderDTO.getTransactionId(), transaction.getTransactionId());
        assertNotNull(transaction.getPaymentOrderCreationDate());
        assertNotNull(transaction.getStatus());
    }

    @Test
    public void submitPaymentDuplicateTransactionIdError() throws IOException {
        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        String transactionId = "TXN1";
        paymentOrderDTO.setTransactionId(transactionId);
        paymentOrderDTO.setApplicationRefId("APPPAYMENT"+Math.random());
        paymentOrderDTO.setAmount(BigDecimal.valueOf(5000));
        paymentOrderDTO.setRecipientAccountId("ACCOUNT2");
        paymentOrderDTO.setSenderAccountId("ACCOUNT1");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();
        http.newCall(request).execute();

        Response response = http.newCall(request).execute();
        assertEquals(400, response.code());
        assertEquals("Transaction Id already exists", response.body().string());
    }

    @Test
    public void submitPaymentExistingSuccesfullAppRefIdError() throws IOException {
        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        String transactionId = "TXN1";
        paymentOrderDTO.setTransactionId(transactionId);
        paymentOrderDTO.setApplicationRefId("APPPAYMENT"+Math.random());
        paymentOrderDTO.setAmount(BigDecimal.valueOf(5000));
        paymentOrderDTO.setRecipientAccountId("ACCOUNT2");
        paymentOrderDTO.setSenderAccountId("ACCOUNT1");

        //First request
        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();
        http.newCall(request).execute();

        Transaction transaction = di.transactionDAO().find("TXN1");
        transaction.setStatus(TransactionStatus.SUCCESFULL);
        di.transactionDAO().update(transaction);

        paymentOrderDTO.setTransactionId("TXN2");

        //Second request
        body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();
        Response response = http.newCall(request).execute();
        assertEquals(400, response.code());
        assertEquals("Double Payment not allowed", response.body().string());
    }

    @Test
    public void submitPaymentInvalidSenderAccountError() throws IOException {
        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        String transactionId = "TXN"+ LocalDateTime.now().toString();
        paymentOrderDTO.setTransactionId(transactionId);
        paymentOrderDTO.setApplicationRefId("APPPAYMENT"+Math.random());
        paymentOrderDTO.setAmount(BigDecimal.valueOf(5000));
        paymentOrderDTO.setRecipientAccountId("ACCOUNT2");
        paymentOrderDTO.setSenderAccountId("ACCOUNT5");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();

        Response response = http.newCall(request).execute();
        assertEquals(400, response.code());
        assertEquals("Invalid Sender Account Id", response.body().string());
    }

    @Test
    public void submitPaymentInvalidRecipientAccountError() throws IOException {
        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        String transactionId = "TXN"+ LocalDateTime.now().toString();
        paymentOrderDTO.setTransactionId(transactionId);
        paymentOrderDTO.setApplicationRefId("APPPAYMENT"+Math.random());
        paymentOrderDTO.setAmount(BigDecimal.valueOf(5000));
        paymentOrderDTO.setRecipientAccountId("ACCOUNT5");
        paymentOrderDTO.setSenderAccountId("ACCOUNT1");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();

        Response response = http.newCall(request).execute();
        assertEquals(400, response.code());
        assertEquals("Invalid Recipient Account Id", response.body().string());
    }

    @Test
    public void submitPaymentInsufficientFundsAccountError() throws IOException {

        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        String transactionId = "TXN"+ LocalDateTime.now().toString();
        paymentOrderDTO.setTransactionId(transactionId);
        paymentOrderDTO.setApplicationRefId("APPPAYMENT"+Math.random());
        paymentOrderDTO.setAmount(BigDecimal.valueOf(100000));
        paymentOrderDTO.setRecipientAccountId("ACCOUNT2");
        paymentOrderDTO.setSenderAccountId("ACCOUNT1");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();

        Response response = http.newCall(request).execute();
        assertEquals(400, response.code());
        assertEquals("Insufficient Funds", response.body().string());
    }

    @Test
    public void submitPaymentNegativePaymentAmountError() throws IOException {

        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        String transactionId = "TXN"+ LocalDateTime.now().toString();
        paymentOrderDTO.setTransactionId(transactionId);
        paymentOrderDTO.setApplicationRefId("APPPAYMENT"+Math.random());
        paymentOrderDTO.setAmount(BigDecimal.valueOf(-50000));
        paymentOrderDTO.setRecipientAccountId("ACCOUNT2");
        paymentOrderDTO.setSenderAccountId("ACCOUNT1");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();

        Response response = http.newCall(request).execute();
        assertEquals(400, response.code());
        assertEquals("Invalid payment amount cannot be negative", response.body().string());
    }

    @Test
    public void submitPaymentEnquiry() throws IOException {

        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        String transactionId = "TXN"+ LocalDateTime.now().toString();
        paymentOrderDTO.setTransactionId(transactionId);
        String applicationRefId = "APPPAYMENT"+Math.random();
        paymentOrderDTO.setApplicationRefId(applicationRefId);
        paymentOrderDTO.setAmount(BigDecimal.valueOf(5000));
        paymentOrderDTO.setRecipientAccountId("ACCOUNT2");
        paymentOrderDTO.setSenderAccountId("ACCOUNT1");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();

        Response response = http.newCall(request).execute();
        assertEquals(200, response.code());


        request = new Request.Builder().url("http://localhost:8080/payment/enquire?applicationRefId=" + applicationRefId).get().build();
        response = http.newCall(request).execute();
        assertEquals(200, response.code());
        ReceiptDTO receiptDTO = objectMapper.readValue(response.body().string() , ReceiptDTO.class);
        assertNotNull(receiptDTO);
    }

    @Test
    public void submitPaymentRefundRequestUnknownPaymentId() {
        logger.warn("Unimplemented");
    }

    @Test
    public void submitPaymentRefundRequest() {
        logger.warn("Unimplemented");
    }

    @Test
    public void finalizePayment() throws IOException, InterruptedException {
        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        String transactionId = "TXN"+ LocalDateTime.now().toString();
        paymentOrderDTO.setTransactionId(transactionId);
        String appRefId = "APPPAYMENT"+Math.random();
        paymentOrderDTO.setApplicationRefId(appRefId);
        paymentOrderDTO.setAmount(BigDecimal.valueOf(5000));
        paymentOrderDTO.setRecipientAccountId("ACCOUNT2");
        paymentOrderDTO.setSenderAccountId("ACCOUNT1");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();

        Response response = http.newCall(request).execute();
        assertEquals(200, response.code());
        PaymentOrderDTO responsePaymentOrderDTO = objectMapper.readValue(response.body().string(), PaymentOrderDTO.class);
        String receiptToken = responsePaymentOrderDTO.getReceiptToken();
        assertNotNull(receiptToken);

        Thread.sleep(2000);

        request = new Request.Builder().url("http://localhost:8080/payment/finalize?transactionId=" + transactionId +
                                            "&receiptToken=" + receiptToken).get().build();
        response = http.newCall(request).execute();
        assertEquals(200, response.code());
        ReceiptDTO receiptDTO = objectMapper.readValue(response.body().string() , ReceiptDTO.class);
        assertEquals(transactionId, receiptDTO.getTransactionId());
        assertEquals(appRefId, receiptDTO.getApplicationRefId());
        assertEquals(BigDecimal.valueOf(5000), receiptDTO.getAmount());

        Transaction transaction = di.transactionDAO().findByApplicationRefIdAndStatus(appRefId, TransactionStatus.SUCCESFULL);
        assertNotNull(transaction);
        assertNotNull(transaction.getPaymentId());
        assertEquals(transaction.getPaymentId(), receiptDTO.getPaymentId());

        BigDecimal balance2 = di.ledgerDAO().getAll("ACCOUNT2").stream().map( ledgerUpdate -> {
            assertEquals(LedgerUpdateStatus.FINAL, ledgerUpdate.getStatus());
            return ledgerUpdate.getBalanceUpdate();
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(BigDecimal.valueOf(55000), balance2);
        BigDecimal balance1 = di.ledgerDAO().getAll("ACCOUNT1").stream().map( ledgerUpdate -> {
            assertEquals(LedgerUpdateStatus.FINAL, ledgerUpdate.getStatus());
            return ledgerUpdate.getBalanceUpdate();
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(BigDecimal.valueOf(45000), balance1);
    }

    @Test
    public void finalizePaymentInvalidTransactionId() throws IOException, InterruptedException {
        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        String transactionId = "TXN"+ LocalDateTime.now().toString();
        paymentOrderDTO.setTransactionId(transactionId);
        String appRefId = "APPPAYMENT"+Math.random();
        paymentOrderDTO.setApplicationRefId(appRefId);
        paymentOrderDTO.setAmount(BigDecimal.valueOf(5000));
        paymentOrderDTO.setRecipientAccountId("ACCOUNT2");
        paymentOrderDTO.setSenderAccountId("ACCOUNT1");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();

        Response response = http.newCall(request).execute();
        assertEquals(200, response.code());
        PaymentOrderDTO responsePaymentOrderDTO = objectMapper.readValue(response.body().string(), PaymentOrderDTO.class);
        String receiptToken = responsePaymentOrderDTO.getReceiptToken();
        assertNotNull(receiptToken);

        Thread.sleep(2000);

        request = new Request.Builder().url("http://localhost:8080/payment/finalize?transactionId=" + "invalidTransactionId" +
                "&receiptToken=" + receiptToken).get().build();
        response = http.newCall(request).execute();
        assertEquals(404, response.code());
        assertEquals("Not Found", response.body().string());
    }

    @Test
    public void finalizePaymentInvalidReceiptToken() throws IOException, InterruptedException {
        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        String transactionId = "TXN"+ LocalDateTime.now().toString();
        paymentOrderDTO.setTransactionId(transactionId);
        String appRefId = "APPPAYMENT"+Math.random();
        paymentOrderDTO.setApplicationRefId(appRefId);
        paymentOrderDTO.setAmount(BigDecimal.valueOf(5000));
        paymentOrderDTO.setRecipientAccountId("ACCOUNT2");
        paymentOrderDTO.setSenderAccountId("ACCOUNT1");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();

        Response response = http.newCall(request).execute();
        assertEquals(200, response.code());
        PaymentOrderDTO responsePaymentOrderDTO = objectMapper.readValue(response.body().string(), PaymentOrderDTO.class);
        String receiptToken = responsePaymentOrderDTO.getReceiptToken();
        assertNotNull(receiptToken);

        Thread.sleep(2000);

        request = new Request.Builder().url("http://localhost:8080/payment/finalize?transactionId=" + transactionId +
                "&receiptToken=" + "invalidReceiptToken").get().build();
        response = http.newCall(request).execute();
        assertEquals(400, response.code());
        assertEquals("Invalid Receipt Token", response.body().string());
    }

    @Test
    public void finalizePaymentStatusStillInProgress() throws Exception {

        main.getCamelContext().removeRoute("ledger");

        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        String transactionId = "TXN"+ LocalDateTime.now().toString();
        paymentOrderDTO.setTransactionId(transactionId);
        String appRefId = "APPPAYMENT"+Math.random();
        paymentOrderDTO.setApplicationRefId(appRefId);
        paymentOrderDTO.setAmount(BigDecimal.valueOf(5000));
        paymentOrderDTO.setRecipientAccountId("ACCOUNT2");
        paymentOrderDTO.setSenderAccountId("ACCOUNT1");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(paymentOrderDTO), MediaType.get("application/json"));
        Request request = new Request.Builder().url("http://localhost:8080/payment").post(body).build();

        Response response = http.newCall(request).execute();
        assertEquals(200, response.code());
        PaymentOrderDTO responsePaymentOrderDTO = objectMapper.readValue(response.body().string(), PaymentOrderDTO.class);
        String receiptToken = responsePaymentOrderDTO.getReceiptToken();
        assertNotNull(receiptToken);

        request = new Request.Builder().url("http://localhost:8080/payment/finalize?transactionId=" + transactionId +
                "&receiptToken=" + receiptToken).get().build();
        response = http.newCall(request).execute();
        assertEquals(200, response.code());
        ReceiptDTO receiptDTO = objectMapper.readValue(response.body().string() , ReceiptDTO.class);
        assertEquals(TransactionStatus.INPROGRESS, receiptDTO.getStatus());

        main.addRouteBuilder(new LedgerRouteBuilder());

        Thread.sleep(1000);

        response = http.newCall(request).execute();
        assertEquals(200, response.code());
        receiptDTO = objectMapper.readValue(response.body().string() , ReceiptDTO.class);
        assertEquals(TransactionStatus.SUCCESFULL, receiptDTO.getStatus());
    }

    @Test
    public void cancelPayment() {
       logger.warn("Unimplemented");
    }

    @Test
    public void cancelPaymentIsFinalError() {
        logger.warn("Unimplemented");
    }

    @BeforeClass
    public static void startProcess() throws Exception {
        TestProcess.getInstance().startIfNotRunning();
    }

    @AfterClass
    public static void stop() throws InterruptedException {
        Thread.sleep(3000);
    }

}
