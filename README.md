## Lagom Basket Service

This is an implementation for a simple HTTP service (with Java & maven) using Lagom framework https://www.lagomframework.com
This service is implemented using the ES-CQRS pattern

To interact with this service:
```
1- `GET` request to `/api/basket/{uuid}` to get the full user basket info
```

and return DONE when adding a new item to the user basket using a 
```
2- `PUT` request to `/api/basket/{uuid}` to add or replace an item in the user basket
```

```
3- Check your cassendra DB to view how Lagom saved your events.
```

### How to use:

from cmd go to your repo directory and run the service using this maven command, Lagom will run everything for you like cassendra DB, API Gateway with hot reload amazing feature.
```
mvn lagom:runAll
```

You can start to GET a basket info using any random UUID 
```
curl http://localhost:9000/api/basket/a0e55f06-7f2a-4b18-a512-d83ed82b8026
```

The basket will be empty
```
{
  "uuid": "a0e55f06-7f2a-4b18-a512-d83ed82b8026",
  "userUuid": "",
  "items": [],
  "subTotal": "0.0",
  "tax": "0.0",
  "total": "0.0"
}
```
So lets add a new item to the basket (for simplicity I added userUuid within the PUT body, ideally it should be retrieved from Request Header for identification and authintication purposes)
```
curl -H "Content-Type: application/json" -d "{\"userUuid\":\"73947738-d5f4-453c-b476-6ac6ab6fb00e\" ,\"itemId\": \"c9f3c98b-e680-4090-bfac-c60aca3d1db7\",\"quantity\": 2,\"price\": 10}" -X PUT http://localhost:9000/api/basket/a0e55f06-7f2a-4b18-a512-d83ed82b8026
```
you will get
```
{ "done" : true }
```
Then try to run the same command but with a new ItemUUID , QTY and price (if the same it will just replace)
```
curl -H "Content-Type: application/json" -d "{\"userUuid\":\"73947738-d5f4-453c-b476-6ac6ab6fb00e\" ,\"itemId\": \"c9f3c98b-e680-4090-bfac-c60aca3d1d88\",\"quantity\": 3,\"price\": 20}" -X PUT http://localhost:9000/api/basket/a0e55f06-7f2a-4b18-a512-d83ed82b8026
```
Then you will get the following json representing the basket content after using the above GET command again.

```
{
  "uuid": "a0e55f06-7f2a-4b18-a512-d83ed82b8026",
  "userUuid": "73947738-d5f4-453c-b476-6ac6ab6fb00e",
  "items": [
    {
      "uuid": "c9f3c98b-e680-4090-bfac-c60aca3d1db7",
      "quantity": "2",
      "price": "10.0"
    },
    {
      "uuid": "c9f3c98b-e680-4090-bfac-c60aca3d1d88",
      "quantity": "3",
      "price": "20.0"
    }
  ],
  "subTotal": "80.0",
  "tax": "5.0",
  "total": "85.0"
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




