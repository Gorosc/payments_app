package org.cgoro.mappers;

import org.cgoro.db.entity.Transaction;
import org.cgoro.model.PaymentOrderDTO;
import org.cgoro.model.ReceiptDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    Transaction paymentOrderToTransaction(PaymentOrderDTO paymentOrderDTO);
    ReceiptDTO transactionToReceiptDTO(Transaction transaction);
}
