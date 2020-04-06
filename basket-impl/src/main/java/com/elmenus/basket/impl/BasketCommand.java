package com.elmenus.basket.impl;

import akka.actor.typed.ActorRef;
import com.elmenus.basket.api.BasketItem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.List;
import java.util.UUID;

/**
  * This interface defines all the commands that the Basket aggregate supports.
  * <p>
  * By convention, the commands and replies should be inner classes of the
  * interface, which makes it simple to get a complete picture of what commands
  * an aggregate supports.
  */
public interface BasketCommand extends Jsonable {

  @Value
  @JsonDeserialize
  static final class AddItemCommand implements BasketCommand, CompressedJsonable {
    public final String userUuid;
    public final String itemUuid;
    public final String quantity;
    public final String price;
    public final ActorRef<Confirmation> replyTo;

    @JsonCreator
    AddItemCommand(String userUuid ,String itemUuid, String quantity, String price, ActorRef<Confirmation> replyTo) {
      this.userUuid = Preconditions.checkNotNull(userUuid, "user UUID is missing");
      this.itemUuid = Preconditions.checkNotNull(itemUuid, "item UUID is missing");
      this.quantity = quantity;
      this.price = price;
      this.replyTo = replyTo;
    }
  }


  @Value
  @JsonDeserialize
  static final class GetBasketCommand implements BasketCommand, CompressedJsonable {
    public final ActorRef<Summary> replyTo;

    @JsonCreator
    GetBasketCommand(ActorRef<Summary> replyTo) {
      this.replyTo = replyTo;
    }
  }

  // The commands above will use different reply types (see below all the reply types).
  // User Basket Replies
  //
  interface Reply extends Jsonable {}

  /**
   * Super interface for Accepted/Rejected replies used by UseGreetingMessage
   */
  interface Confirmation extends Reply {}

  @Value
  @JsonDeserialize
  static final class Summary implements Reply {

    public final List<ItemDTO> items;
    public final String userID ;
    public final float subTotal;
    public final float tax;
    public final float total;

    @JsonCreator
    Summary(List<ItemDTO> items, String userID, float subTotal, float tax , float total) {
      this.items = items;
      this.userID=userID;
      this.subTotal=subTotal;
      this.tax=tax;
      this.total=total;
    }
  }

  @Value
  @JsonDeserialize
  static final class Accepted implements Confirmation {
    public final Summary summary;

    @JsonCreator
    Accepted(Summary summary) {
      this.summary = summary;
    }

    @JsonCreator
    Accepted() {
      this.summary = null;
    }


  }

  @Value
  @JsonDeserialize
  static final class Rejected implements Confirmation {
    public final String reason;

    @JsonCreator
    Rejected(String reason) {
      this.reason = reason;
    }
  }


}
