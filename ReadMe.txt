Payments App
============

The flow for the payment designed utilizes a two phase payment procedure (submit / finalize) and a background process that serially processes submitted payment orders.
Thus double payment and concurrency issues are eliminated. The second phase of the payment procedure verifies and finalized the outcome of the background process

The balance of the accounts is kept in a ledger where each balance update is saved and is mapped to a successful submitted payment order. The balance of each account is the
sum of his balance update since the beginning of time. Thus we further eliminate any inconsistencies in balance movements because the complete history is visible and audible.

In details the flow is the following:

1. A Payment Order is submitted via a post request to  /payment
    The payment order has the following significant fields:
     - Transaction Id: Set by the external system sets a unique id for the transaction
     - Application Ref Id: Set by the external system sets a business related id for the transactions. The application ref Id
       is unique for successful payments only. f.e. you can have a failed transaction and a succesfull transaction wit the same app ref id
       but not two successful ones.
2. A Transaction is created in the system with the details of the Payment Order and status InProgress
    The transaction has further significant fields that are set on a later stage:
     - Payment Id: Set by the payment system is the id of a validated payment.
     - Receipt: For each transaction that is commited a receipt is created with a receipt Token
3. The payment system responds with the Payment Order filld with the receipt Token. This will be used from the external system to finalize the payment

4. The ledgerUpdate process is executed in small intervals settling in progress transactions. It catches the in progress transaction, validates it for sufficient funds
  and created two ledger updates one for the sender and one for the recipient. The LedgerUpdate thas the following significant fields:
  Transaction Id : maps to a transactions
  Account Id: maps to an account
  Update: the movement of the balance can be positive or negative
  Status: Can be FINAL, PENDING, REJECTED, CANCELLED.
  At this moment in the flow the ledget updates have the Status PENDING meaning that they are counted towards the balance of the account but they are not yet finalized. Ledger
  Updates stay in this status until the external system finalizes or a predefined interval passes where they are automatically cancelled.
  They can also be explicitly cancelled by the external system.

5. The external systems finalizes the payment by a get requets to /payment/finalize with query params the transaction id and the receipt token received from the first step
  The payment system will validate the request and at this point will set the Ledger Updates status to FINAL and creates a payment Id that will act as the Invoice number for the payment. This payment can no longer be cancelled
  The payment system will respond with the information of the transaction no matter the Transaction Status (can be In Progress or Rejected) so it will also be notified for payment failures

5b. The external system cancels the payment (which is not yet finalized) and the LedgerUpdates are marked as cancelled.

6. After finalization the external system can request a refund of the payment.
