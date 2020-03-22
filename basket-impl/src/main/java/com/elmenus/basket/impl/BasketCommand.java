package com.elmenus.basket.impl;

import akka.actor.typed.ActorRef;
import com.elmenus.basket.api.ItemDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

/**
  * This interface defines all the commands that the Basket aggregate supports.
  * <p>
  * By convention, the commands and replies should be inner classes of the
  * interface, which makes it simple to get a complete picture of what commands
  * an aggregate supports.
  */
public interface BasketCommand extends Jsonable {

  /**
  * A command to switch the greeting message.
  * <p>
  * It has a reply type of {@link Confirmation}, which is sent back to the caller
  * when all the events emitted by this command are successfully persisted.
  */
  @SuppressWarnings("serial")
  @Value
  @JsonDeserialize
  final class UseGreetingMessage implements BasketCommand, CompressedJsonable {
    public final String message;
    public final ActorRef<Confirmation> replyTo;
    
    @JsonCreator
    UseGreetingMessage(String message, ActorRef<Confirmation> replyTo) {
      this.message = Preconditions.checkNotNull(message, "message");
      this.replyTo = replyTo;
    }
  }

  /**
  * A command to say hello to someone using the current greeting message.
  * <p>
  * The reply type is {@link Greeting} and will contain the message to say to that
  * person.
  */
  @SuppressWarnings("serial")
  @Value
  @JsonDeserialize
  final class Hello implements BasketCommand {
    public final String name;
    public final ActorRef<Greeting> replyTo;
    
    @JsonCreator
    Hello(String name, ActorRef<Greeting> replyTo) {
      this.name = Preconditions.checkNotNull(name, "name");
      this.replyTo = replyTo;
    }
  }

  @Value
  @JsonDeserialize
  static final class AddItemCommand implements BasketCommand, CompressedJsonable {
    public final UUID itemId;
    public final UUID userId;
    public final int quantity;
    public final float price;
    public final ActorRef<Confirmation> replyTo;

    @JsonCreator
    AddItemCommand(UUID itemId, UUID userId , int quantity, float price, ActorRef<Confirmation> replyTo) {
      this.itemId = Preconditions.checkNotNull(itemId, "itemId is missing");
      this.userId = Preconditions.checkNotNull(userId, "user Id is missing");
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
  



  /**
  * Reply type for a Hello command.
  */
  @Value
  @JsonDeserialize
  final class Greeting {
    public final String message;
    
    public Greeting(String message) {

      this.message = message;
    }
  }

  //
  // SHOPPING CART REPLIES
  //
  interface Reply extends Jsonable {}

  /**
   * Super interface for Accepted/Rejected replies used by UseGreetingMessage
   */
  interface Confirmation extends Reply {}

  @Value
  @JsonDeserialize
  static final class Summary implements Reply {

    public final Set<ItemDTO> items;
    public final String userID ;
    public final float subTotal;
    public final float tax;
    public final float total;


    @JsonCreator
    Summary(Set<ItemDTO> items, String userID, float subTotal, float tax , float total) {
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
