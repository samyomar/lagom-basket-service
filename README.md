## Lagom Basket Service

This is an implementation for a simple HTTP service (with Java & maven) using Lagom framework https://www.lagomframework.com
This service is implemented using the ES-CQRS pattern with cassendra as the backend DB.

#### Lagom Service High Level Architecture
Lagom Persistence makes use of Event sourcing and CQRS to help achieve the decoupled architecture, So for any write operations you need to send commands, which may result in an event then the event handler update the state, for write operations in Lagom you have to implement [Read-Side Processor](https://blog.knoldus.com/persistent-read-side-lagom/)

![alt text](https://divyadua25.files.wordpress.com/2018/06/cqrss.png?resize=744%2C520)

In our simple example we used a command for adding new item (write operation) and also we used a readonly command to read the status of the basket, for further complex or cross table queries you have to implement the read side processor.

#### What has been implemented ?
* Basket service to get the status of the basket (using GET) and to add new items to the basket (using PUT).
* Unit tests, some unit tests examples implemneted to cover the business scanarios (in class BasketServiceTesting)
* Business Validations (no user can add items to other users baskets, validations on QTY and Price, UUIDs should be valid)
* Reading userUuid from request header (without authintication/authorization).

This small microservice contains 2 services:
```
1- `GET` --> `/api/basket/{uuid}` to get the full user basket info (getBasket service)
```
```
2- `PUT` --> `/api/basket/{uuid}` to add or replace an item to the user basket (addItem service)
```

### How to use:

from cmd go to your repo directory and run the service using this maven command, Lagom will run everything for you like cassendra DB, API Gateway with hot reload amazing feature.
(in windows you might need to kill any process listining on port 9000 or change lagom 9000)
to find the procees id run on terminal this command   
```
netstat -ano | findstr :9000
```
then use this PID number in this command  
```
taskkill /PID <PID_Value> /F   (This must be run with admin permissions)
```

Start running your Lagom service
```
mvn lagom:runAll
```

GET the basket info using any random UUID 
```
curl http://localhost:9000/api/basket/a0e55f06-7f2a-4b18-a512-d83ed82b8026
```

The basket will be empty
```
{
  "uuid": "a0e55f06-7f2a-4b18-a512-d83ed82b8026",
  "userUuid": "",
  "items": [],
  "subTotal": "0"
  "tax": "0",
  "total": "0"
}
```
So lets add a new item to the basket (for simplicity I added userUuid within the PUT body, ideally it should be retrieved from Request Header for identification and authintication purposes)
```
curl -H "Content-Type: application/json" 
     -H "UserUuid: 1927a910-5045-43d6-9242-19b8c01a96cc"
     -d "{\"uuid\": \"c9f3c98b-e680-4090-bfac-c60aca3d1db7\",\"quantity\": 2,\"price\": 10}"      
     -X PUT http://localhost:9000/api/basket/a0e55f06-7f2a-4b18-a512-d83ed82b8026
```
you will get
```
{ "done" : true }
```
Then try to run the same command but with a new ItemUUID , QTY and price (if the same it will just replace)
```
curl -H "Content-Type: application/json" 
     -H "UserUuid: 1927a910-5045-43d6-9242-19b8c01a96cc"
     -d "{\"uuid\": \"c9f3c98b-e680-4090-bfac-c60aca3d1d88\",\"quantity\": 3,\"price\": 7}"      
     -X PUT http://localhost:9000/api/basket/a0e55f06-7f2a-4b18-a512-d83ed82b8026
```
run the GET command above again, you will get the basket content like the following

```
{
   "uuid": "a0e55f06-7f2a-4b18-a512-d83ed82b8026",
   "userUuid": "1927a910-5045-43d6-9242-19b8c01a96cc",
   "items": [
      {
         "uuid": "c9f3c98b-e680-4090-bfac-c60aca3d1db7",
         "quantity": "2",
         "price": "10"
      },
      {
         "uuid": "c9f3c98b-e680-4090-bfac-c60aca3d1d88",
         "quantity": "3",
         "price": "7"
      }
   ],
   "subTotal": "41",
   "tax": "4.1",
   "total": "45.1"
}
```

Hint: you can get random UUID simply using this simple curl command
```
curl https://www.uuidgenerator.net/api/version4	)
```

***Note: 
PUT is used to create or update a resource , so if you try to insert the same item multiple times it will only replace it because PUT is idempotent.

#### Assumptions & Future Work:
- UserID value should be taken from Header, for simplicity we send it in PUT body.
- Tax value is hardcoded for simplicity, it should be retrived dynamically from DB, external service or configuration files.
- Communication with other services should be done by publishing an event to Kafka topic, or consumming from other subscribed topics.
- Security checks needs to be implemented (authintication & authorization). So currently if userID is changed for the same basket ID it will just replace it with the new userID (a security validation should be added).
- Unit Testing should be added.
- Business validations should be added like item QTY and price should not be > zero.

#### Prerequisites
to understand Lagom framework properly you need to be familiar with the following concepts:
- Reactive Programming.
- Functional Programming.
- Domain Driven Design (DDD) (bounded context, aggregate , command, state, event)
- Microservices (basic concepts).
- Event Sourcing and QCRS -> https://docs.microsoft.com/en-us/azure/architecture/patterns/cqrs
- Akka https://akka.io/
- Java / scala
- Maven / Sbt

### For researchers
Lagom is a microservices focused framework it provide many out of the box features like ES-CQRS and hot code reload, 
But its not the only player in this domain, here is a list of other frameworks that really deserve a trial and open the door for in depth comparisons.

* Axon Framework
* Quarkus
* Eventuate.io
* axoniq.io
* micronaut.io/
* quarkus.io/
* Spring Boot
* MicroProfile




