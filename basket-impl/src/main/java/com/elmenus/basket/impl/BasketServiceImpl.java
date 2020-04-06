package com.elmenus.basket.impl;

import akka.Done;
import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.japi.Pair;
import com.elmenus.basket.api.*;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import com.elmenus.basket.impl.BasketCommand.*;
import com.lightbend.lagom.javadsl.server.HeaderServiceCall;
import lombok.extern.java.Log;

import javax.inject.Inject;
import java.time.Duration;
import java.util.*;

/**
 * Implementation of the BasketService.
 */
@Log
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
                    .thenApply(summary -> asBasketView(basketUUID.toString(),summary));
  }

  @Override
  public HeaderServiceCall<BasketItem, Done> addItem(UUID basketUUID) {
    ResponseHeader responseHeader = ResponseHeader.OK;
      return (requestHeader,basketItem) -> {
         return entityRef(basketUUID)
                .<BasketCommand.Confirmation>ask(replyTo -> new BasketCommand.AddItemCommand(
                                  requestHeader.getHeader("UserUuid").orElse(null)
                                , basketItem.uuid
                                , basketItem.quantity
                                , basketItem.price
                                , replyTo)
                        , askTimeout)
                .thenApply(this::handleConfirmation)
                .thenApply(accepted -> Pair.create(responseHeader, Done.getInstance()));
      };
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
    List<BasketItem> basketItemList= new ArrayList();
    for(ItemDTO item:summary.items){
      basketItemList.add( new BasketItem(item.itemUuid,formatToString(item.quantity),formatToString(item.price)));
    }
    return new BasketView(id, summary.userID,basketItemList, formatToString(summary.subTotal),formatToString(summary.tax),formatToString(summary.total));
  }


  public static String formatToString(double d)
  {
    if(d == (long) d)
      return String.format("%d",(long)d);
    else
      return String.format("%s",d);
  }



}
