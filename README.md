## Lagom Basket Service
A simple user basket microservice implementation using Lagom framework.

This is an implementation for an HTTP service (using Java) with Lagom framework https://www.lagomframework.com:
This service return the full user basket as a response to a
```
`GET` request to `/api/basket/{uuid}`
```

and return DONE when adding a new item to the user basket using a 
```
`PUT` request to `/api/basket/{uuid}` 
```

## How to use:

from CMD go to your repo directory and run the service --> mvn lagom:runAll

to GET basket info            -->  
```
curl http://localhost:9000/api/basket/c78383b8-208d-4a3b-a709-1cbc463dd541
```

to Add new item to the basket -->  
```
curl -H "Content-Type: application/json" -d '{"uuid": "c9f3c98b-e680-4090-bfac-c60aca3d1db7","quantity": "2","price": "10"}' -X PUT http://localhost:9000/api/basket/c78383b8-208d-4a3b-a709-1cbc463dd541
```

Hint: you can get random UUID using
```
curl https://www.uuidgenerator.net/api/version4	)
```

Note: 
PUT is used to create or update a resource , so if you try to insert the same item multible times it will only replace it because PUT is idempotent.

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


## Prerequisites
to understand Lagom framework properly you need to be familiar with the following concepts:
- Reactive Programming.
- Functional Programming.
- Microservices (basic concepts).
- Event Sourcing and QCRS -> https://docs.microsoft.com/en-us/azure/architecture/patterns/cqrs
- Akka https://akka.io/


