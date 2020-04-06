package com.elmenus.basket.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.lightbend.lagom.javadsl.server.ServerServiceCall;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 * The Basket service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the Basket.
 */
public interface BasketService extends Service {


  ServiceCall<NotUsed, BasketView> getBasket(UUID basketUUID);
  ServiceCall<BasketItem, Done> addItem(UUID basketUUID);


  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("basket").withCalls(
            restCall(Method.GET, "/api/basket/:basketUUID",this::getBasket),
            restCall(Method.PUT, "/api/basket/:basketUUID",this::addItem)
      ).withTopics().withAutoAcl(true);
  }
}
