package com.elmenus.basket.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 * The Basket service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the Basket.
 */
public interface BasketService extends Service {


  ServiceCall<NotUsed, String> hello(String id);
  ServiceCall<GreetingMessage, Done> useGreeting(String id);


  ServiceCall<NotUsed, BasketView> getBasket(UUID basketUUID);
  ServiceCall<BasketItem, Done> addItem(UUID basketUUID);

  /**
   * This gets published to Kafka.
   */
  //Topic<BasketEvent> helloEvents();

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("basket").withCalls(
            restCall(Method.GET, "/api/basket/:basketUUID",this::getBasket),
            restCall(Method.PUT, "/api/basket/:basketUUID",this::addItem),

            restCall(Method.GET, "/api/basket/hello/:id",this::hello),
            restCall(Method.POST, "/api/basket/hello/:id",this::useGreeting)
      ).withTopics(
          //topic("hello-events", this::helloEvents)
          // Kafka partitions messages, messages within the same partition will
          // be delivered in order, to ensure that all messages for the same user
          // go to the same partition (and hence are delivered in order with respect
          // to that user), we configure a partition key strategy that extracts the
          // name as the partition key.
         // .withProperty(KafkaProperties.partitionKeyStrategy(), BasketEvent::getName)
        ).withAutoAcl(true);
    // @formatter:on
  }
}
