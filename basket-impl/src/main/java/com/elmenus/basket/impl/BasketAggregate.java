package com.elmenus.basket.impl;

import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import com.lightbend.lagom.javadsl.persistence.AkkaTaggerAdapter;
import lombok.extern.java.Log;
import java.util.Set;

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

    /*
     * Command handler for the AddItem command.
     */
    builder.forAnyState()
            .onCommand(BasketCommand.AddItemCommand.class, (state, cmd) ->
                    Effect()
                            // In response to this command, we want to first persist it as a
                            // GreetingMessageChanged event
                            .persist(new BasketEvent.ItemAddedEvent(entityId,cmd.userId.toString(),cmd.itemId.toString(),cmd.quantity,cmd.price,getTaxValue()))
                            // Then once the event is successfully persisted, we respond with done.
                            .thenReply(cmd.replyTo, s -> new BasketCommand.Accepted(toSummary(s)))
            );

    builder.forAnyState()
            .onCommand(BasketCommand.GetBasketCommand.class, (state, cmd) ->
             Effect().none()
                     .thenReply(cmd.replyTo, s -> toSummary(s))
            );

    return builder.build();

  }


  @Override
  public EventHandler<BasketState, BasketEvent> eventHandler() {
    EventHandlerBuilder<BasketState, BasketEvent> builder = newEventHandlerBuilder();

    /*
     * Event handler for the GreetingMessageChanged event.
     */
    builder.forAnyState()
      .onEvent(BasketEvent.ItemAddedEvent.class, (state, evt) ->
        // We simply update the current state to use the greeting message from
        // the event.
        state.addOrUpdateItem(evt.userUuid,evt.itemUuid,evt.quantity,evt.price,evt.tax)
      );
    return builder.build();
  }


  @Override
  public Set<String> tagsFor(BasketEvent shoppingCartEvent) {
    return AkkaTaggerAdapter.fromLagom(entityContext, BasketEvent.TAG).apply(shoppingCartEvent);
  }


  private BasketCommand.Summary toSummary(BasketState basketState) {
    log.severe("basketstate before being summary" + basketState.toString());
    return new BasketCommand.Summary(basketState.basketItems,basketState.userUuid,basketState.subTotal,basketState.tax,basketState.total);
  }

  float getTaxValue()
  {
     // for demo purposes this value is hardcoded here, it should come from a DB or from a service lookup.
     return 5;
  }

}
