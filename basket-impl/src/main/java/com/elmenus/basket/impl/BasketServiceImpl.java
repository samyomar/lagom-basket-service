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
    return new BasketView(id, summary.userID,summary.items, summary.subTotal+"",summary.tax+"",summary.total+"");
  }


}
