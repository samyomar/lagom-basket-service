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
curl http://localhost:9000/api/basket/c78383b8-208d-4a3b-a709-1cbc463dd541
```

The basket will be empty, So lets add a new item to the basket (use any random UUID for userID and ItemID)
```
curl -H "Content-Type: application/json" -d '{"uuid": "c9f3c98b-e680-4090-bfac-c60aca3d1db7","quantity": "2","price": "10"}' -X PUT http://localhost:9000/api/basket/c78383b8-208d-4a3b-a709-1cbc463dd541
```

Then try to run the same command with a new ItemUUID , QTY and price (if the same it will just replace)
```
curl -H "Content-Type: application/json" -d '{"uuid": "3eee6d5d-df32-451d-84b9-6624af588b05","quantity": "3","price": "30"}' -X PUT http://localhost:9000/api/basket/c78383b8-208d-4a3b-a709-1cbc463dd541
```
Then use get command to check the basket info, it should be filled with your items


Hint: you can get random UUID simply using this simple curl command
```
curl https://www.uuidgenerator.net/api/version4	)
```

***Note: 
PUT is used to create or update a resource , so if you try to insert the same item multiple times it will only replace it because PUT is idempotent.

### Basket structure:
      {
        "uuid": "c78383b8-208d-4a3b-a709-1cbc463dd541",
        "userUuid": "72081821-f9e5-4cc8-876b-84f67ad83156",
        "items": [
          {
            "uuid": "c9f3c98b-e680-4090-bfac-c60aca3d1db7",
            "quantity": "2",
            "price": "10"
          },
          {
            "uuid": "d60b5276-16a2-4aa2-9df7-dd09eba10171",
            "quantity": "1",
            "price": "30"
          }
        ],
        "subTotal": "50",
        "tax": "5",
        "total": "55"
      }

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




