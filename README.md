# Intra Bank Payment Transfer System

## What is intra bank payment transfer system

Intra bank payment transfer system primary goal is to be a workflow engine for operations within the domain of an
account.

## Intra bank payment transfer system provides below operations

### 1)  getAccountBalance

#### To get the available account balance for particular account Id.

### 2)  getMiniStatement

#### To get the transactions list happened for a particular account Id.

### 3)  transferAmount

#### To transfer the Amount from one account to another account.

### Assignment details :

1) In this system I have defined some pre data in data sql file which will be inserted in h2 database at the startup of
   the application.
2) I have written two integration test classes :
    1) IntraBankPaymentTransferSystemServiceImplTest : This test will run with the internal h2 database without mock.
    2) IntraBankPaymentTransferSystemMockServiceImplTest : This test will run without internal h2 data like use mock
       implementation class just for service class.
3) I have covered most of the unit tests where required.
4) I have defined swagger UI for rest api documentation.
   use this link http://localhost:8080/swagger-ui.html
5) I have defined profile mockService which can be used to get the mock data from the service but remember it has only
   hardcoded data, by default it will not be activated (!mockService).

Version 1.0  
Date: 05-02-2021  
Author: Praveen Palled  
MailID: praveen8959@gmail.com

