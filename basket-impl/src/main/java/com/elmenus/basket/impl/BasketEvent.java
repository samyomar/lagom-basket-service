package com.elmenus.basket.impl;

import com.elmenus.basket.api.ItemDTO;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.Jsonable;

/**
 * This interface defines all the events that the Basket aggregate supports.
 * <p>
 * By convention, the events should be inner classes of the interface, which
 * makes it simple to get a complete picture of what events an entity has.
 */
public interface BasketEvent extends Jsonable, AggregateEvent<BasketEvent> {

  /**
   * Tags are used for getting and publishing streams of events. Each event
   * will have this tag, and in this case, we are partitioning the tags into
   * 4 shards, which means we can have 4 concurrent processors/publishers of
   * events.
   */
  AggregateEventShards<BasketEvent> TAG = AggregateEventTag.sharded(BasketEvent.class, 4);

  /**
   * An event that represents a change in greeting message.
   */
  @SuppressWarnings("serial")
  @Value
  @JsonDeserialize
  public final class GreetingMessageChanged implements BasketEvent {

    public final String name;
    public final String message;

    @JsonCreator
    public GreetingMessageChanged(String name, String message) {
      this.name = Preconditions.checkNotNull(name, "name");
      this.message = Preconditions.checkNotNull(message, "message");
    }
  }

  /**
   * An event that represents adding new item to the user basket.
   */
  @SuppressWarnings("serial")
  @Value
  @JsonDeserialize
  public final class ItemAddedEvent implements BasketEvent {

    public final String uuid;
    public final String userUuid;
    public final ItemDTO itemDto;
    //public final String itemId;
   // public final int quantity;
   // public final float price;

    @JsonCreator
    public ItemAddedEvent(String uuid,  String userUuid , String itemId, int quantity, float price) {
       this.uuid= uuid;
       this.userUuid=userUuid;
       this.itemDto=new ItemDTO(itemId,quantity,price);
    }

  /*  @JsonCreator
    public ItemAddedEvent(String uuid, String userUuid, ItemDTO itemDto) {
      this.uuid = uuid;
      this.userUuid = userUuid;
      this.itemDto = itemDto;
    }
*/

  }




  @Override
  default AggregateEventTagger<BasketEvent> aggregateTag() {
    return TAG;
  }

}
