package com.elmenus.basket.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
   * An event that represents adding new item to the user basket with the tax value at this moment.
   */
  @SuppressWarnings("serial")
  @Value
  @JsonDeserialize
  public final class ItemAddedEvent implements BasketEvent {

    public final String uuid;
    public final String userUuid;
    public final String itemUuid;
    public final int quantity;
    public final float price;
    public final float taxPercentage;

    @JsonCreator
    public ItemAddedEvent(String uuid,  String userUuid , String itemUuid, int quantity, float price, float taxPercentage) {
       this.uuid= uuid;
       this.userUuid=userUuid;
       this.itemUuid = itemUuid;
       this.quantity=quantity;
       this.price=price;
       this.taxPercentage=taxPercentage;
    }

  }

  @Override
  default AggregateEventTagger<BasketEvent> aggregateTag() {
    return TAG;
  }

}
