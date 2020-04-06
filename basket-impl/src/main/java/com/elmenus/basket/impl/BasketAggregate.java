package com.elmenus.basket.impl;

import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import com.lightbend.lagom.javadsl.persistence.AkkaTaggerAdapter;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import lombok.extern.java.Log;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
* This is an event sourced aggregate. It has a state, {@link BasketState}, which
* stores what the greeting should be (eg, "Hello").
* <p>
* Event sourced aggregate are interacted with by sending them commands. This
* aggregate supports two commands, a {@link BasketCommand.AddItemCommand} command, which is
* used to change the greeting, and a {@link BasketCommand.GetBasketCommand} command, which is a read
* only command which returns a greeting to the name specified by the command.
* <p>
* Commands may emit events, and it's the events that get persisted.
* Each event will have an event handler registered for it, and an
* event handler simply applies an event to the current state. This will be done
* when the event is first created, and it will also be done when the entity is
* loaded from the database - each event will be replayed to recreate the state
* of the aggregate.
* <p>
* This aggregate defines one event, the {@link BasketEvent.ItemAddedEvent} event,
* which is emitted when a {@link BasketCommand.AddItemCommand} command is received.
*/
@Log
public class BasketAggregate extends EventSourcedBehaviorWithEnforcedReplies<BasketCommand, BasketEvent, BasketState> {

  public static EntityTypeKey<BasketCommand> ENTITY_TYPE_KEY =
    EntityTypeKey
      .create(BasketCommand.class, "BasketAggregate");


  final private EntityContext<BasketCommand> entityContext;
  final private String entityId;
  final private float taxPercentage = 10;

  BasketAggregate(EntityContext<BasketCommand> entityContext) {
    super(
      PersistenceId.of(
          entityContext.getEntityTypeKey().name(),
          entityContext.getEntityId()
        )
      );
    this.entityContext = entityContext;
    this.entityId = entityContext.getEntityId();
  }

  public static BasketAggregate create(EntityContext<BasketCommand> entityContext) {
    return new BasketAggregate(entityContext);
  }

  @Override
  public BasketState emptyState() {
    return BasketState.EMPTY;
  }




  @Override
  public CommandHandlerWithReply<BasketCommand, BasketEvent, BasketState> commandHandler() {
    CommandHandlerWithReplyBuilder<BasketCommand, BasketEvent, BasketState> builder = newCommandHandlerWithReplyBuilder();
    //Command handler for the AddItem command.
    builder.forAnyState().onCommand(BasketCommand.AddItemCommand.class, this::onAddOrUpdateItem)
                         .onCommand(BasketCommand.GetBasketCommand.class, this::onGetBasketInfo);
    return builder.build();
  }


  private ReplyEffect<BasketEvent, BasketState> onGetBasketInfo(BasketState basketState, BasketCommand.GetBasketCommand cmd) {
    return Effect().none().thenReply(cmd.replyTo, s -> toSummary(s));
  }

  private ReplyEffect<BasketEvent, BasketState> onAddOrUpdateItem(BasketState basketState, BasketCommand.AddItemCommand cmd) {
    if(cmd.userUuid==null || cmd.userUuid.trim().isEmpty()){
      return Effect().reply(cmd.replyTo, new BasketCommand.Rejected("User ID is missing"));
    }else if (!isUUID(cmd.userUuid)) {
      return Effect().reply(cmd.replyTo, new BasketCommand.Rejected(cmd.userUuid + " User ID is not a valid UUID."));
    } else if (!isUUID(cmd.itemUuid)) {
      return Effect().reply(cmd.replyTo, new BasketCommand.Rejected("Item ID is not a valid UUID."));
    }else if (basketState.hasAnotherUser(cmd.userUuid)) {
      return Effect().reply(cmd.replyTo, new BasketCommand.Rejected("This Basket belongs to another user, Access Denied."));
    } else if(!isNumeric(cmd.quantity)){
      return Effect().reply(cmd.replyTo, new BasketCommand.Rejected("Quantity is not a valid numerical value"));
    } else if(!isNumeric(cmd.price)){
      return Effect().reply(cmd.replyTo, new BasketCommand.Rejected("Price is not a valid numerical value"));
    } else if (Integer.parseInt(cmd.quantity) <= 0) {
      return Effect().reply(cmd.replyTo, new BasketCommand.Rejected("Quantity must be greater than zero"));
    } else if (Float.parseFloat(cmd.getPrice()) < 0) {
      return Effect().reply(cmd.replyTo, new BasketCommand.Rejected("Item Price can not be less than zero"));
    } else {
      return Effect()
              .persist(new BasketEvent.ItemAddedEvent(entityId, cmd.userUuid, cmd.itemUuid, Integer.parseInt(cmd.quantity), Float.parseFloat(cmd.price), getTaxPercentage()))
              .thenReply(cmd.replyTo, s -> new BasketCommand.Accepted(toSummary(s)))
              ;
    }
  }


  @Override
  public EventHandler<BasketState, BasketEvent> eventHandler() {
    EventHandlerBuilder<BasketState, BasketEvent> builder = newEventHandlerBuilder();
    builder.forAnyState()
      .onEvent(BasketEvent.ItemAddedEvent.class, (state, evt) ->
        state.addOrUpdateItem(evt.userUuid,evt.itemUuid,evt.quantity,evt.price,evt.taxPercentage)
      );
    return builder.build();
  }


  @Override
  public Set<String> tagsFor(BasketEvent basketEvent) {
    return AkkaTaggerAdapter.fromLagom(entityContext, BasketEvent.TAG).apply(basketEvent);
  }


  private BasketCommand.Summary toSummary(BasketState basketState) {
    return new BasketCommand.Summary(basketState.items,basketState.userUuid,basketState.subTotal,basketState.taxPercentage,basketState.total);
  }



  float getTaxPercentage()
  {
     // for demo purposes this value is hardcoded here, it should come from a DB or from a service lookup.
    //  this 10% of any subtotal.
     return taxPercentage;
  }

  static boolean isNumeric(String str) {
    try
    {
      Double.parseDouble(str);
      return true;
    } catch(NumberFormatException e)
    {
      return false;
    }
    catch(NullPointerException e)
    {
      return false;
    }
  }

  static boolean isUUID(String string) {
    try {
      UUID temp = UUID.fromString(string);
      return true;
    } catch (Exception ex) {
      log.severe(ex.getMessage());
      ex.printStackTrace();
      return false;
    }
  }


}
