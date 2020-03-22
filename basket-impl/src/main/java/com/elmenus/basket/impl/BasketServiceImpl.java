package com.elmenus.basket.impl;

import akka.Done;
import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import com.elmenus.basket.api.*;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import com.elmenus.basket.impl.BasketCommand.*;

import javax.inject.Inject;
import java.time.Duration;
import java.util.*;

/**
 * Implementation of the BasketService.
 */
public class BasketServiceImpl implements BasketService {

  private final PersistentEntityRegistry persistentEntityRegistry;

  private final Duration askTimeout = Duration.ofSeconds(3);
  private ClusterSharding clusterSharding;

  @Inject
  public BasketServiceImpl(PersistentEntityRegistry persistentEntityRegistry, ClusterSharding clusterSharding){
    this.clusterSharding=clusterSharding;
    // The persistent entity registry is only required to build an event stream for the TopicProducer
    this.persistentEntityRegistry=persistentEntityRegistry;

    // register the Aggregate as a sharded entity
    this.clusterSharding.init(
    Entity.of(
    BasketAggregate.ENTITY_TYPE_KEY,
    BasketAggregate::create
    )
    );
  }


  private EntityRef<BasketCommand> entityRef(String uuid) {
    return clusterSharding.entityRefFor(BasketAggregate.ENTITY_TYPE_KEY, uuid);
  }

  private EntityRef<BasketCommand> entityRef(UUID uuid) {
    return entityRef(uuid.toString());
  }


  @Override
  public ServiceCall<NotUsed, String> hello(String id) {
    return request -> {

    // Look up the aggregete instance for the given ID.
    EntityRef<BasketCommand> ref = clusterSharding.entityRefFor(BasketAggregate.ENTITY_TYPE_KEY, id);
    // Ask the entity the Hello command.

    return ref.
      <BasketCommand.Greeting>ask(replyTo -> new Hello(id, replyTo), askTimeout)
      .thenApply(greeting -> greeting.message);    };
  }

  @Override
  public ServiceCall<GreetingMessage, Done> useGreeting(String id) {
    return request -> {

    // Look up the aggregete instance for the given ID.
    EntityRef<BasketCommand> ref = clusterSharding.entityRefFor(BasketAggregate.ENTITY_TYPE_KEY, id);
    // Tell the entity to use the greeting message specified.

    return ref.
      <BasketCommand.Confirmation>ask(replyTo -> new UseGreetingMessage(request.message, replyTo), askTimeout)
          .thenApply(confirmation -> {
              if (confirmation instanceof BasketCommand.Accepted) {
                return Done.getInstance();
              } else {
                throw new BadRequest(((BasketCommand.Rejected) confirmation).reason);
              }
          });
    };

  }

  @Override
  public ServiceCall<NotUsed, BasketView> getBasket(UUID basketUUID) {
    return request ->
            entityRef(basketUUID)
                    .ask(BasketCommand.GetBasketCommand::new, askTimeout)
                    .thenApply(summary -> asBasketView(basketUUID.toString(), summary));
  }

  @Override
  public ServiceCall<BasketItem, Done> addItem(UUID basketUUID) {
      return basketItem ->  entityRef(basketUUID)
              .<BasketCommand.Confirmation>ask(replyTo -> new BasketCommand.AddItemCommand(
                      basketItem.itemId
                      ,basketItem.userUuid
                      ,basketItem.quantity
                      ,basketItem.price
                      , replyTo)
                      ,askTimeout)
              .thenApply(this::handleConfirmation)
              .thenApply(accepted -> Done.getInstance());
  }



  /**
   * Try to converts Confirmation to a Accepted
   *
   * @throws BadRequest if Confirmation is a Rejected
   */
  private BasketCommand.Accepted handleConfirmation(BasketCommand.Confirmation confirmation) {
    if (confirmation instanceof BasketCommand.Accepted) {
      BasketCommand.Accepted accepted = (BasketCommand.Accepted) confirmation;
      return accepted;
    }
    BasketCommand.Rejected rejected = (BasketCommand.Rejected) confirmation;
    throw new BadRequest(rejected.getReason());
  }

  private BasketView asBasketView(String id, BasketCommand.Summary summary) {
    List<ItemDTO> items = new ArrayList<>();
    for(ItemDTO item :summary.items){items.add(item);}

    return new BasketView(id, summary.userID,items, 0,0,0);
  }


}
