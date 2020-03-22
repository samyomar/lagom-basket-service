package com.elmenus.basket.impl;

import com.elmenus.basket.api.ItemDTO;
import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.extern.java.Log;

import java.util.*;

/**
 * The state for the {@link Basket} aggregate.
 */
@SuppressWarnings("serial")
@Value
@JsonDeserialize
@Log
public final class BasketState implements CompressedJsonable {

  public static final BasketState EMPTY = new BasketState("", Collections.emptySet(), 0,0,0);

 // public static final BasketState INITIAL = new BasketState("Hello", LocalDateTime.now().toString());
  public final String userUuid;
  public final Set<ItemDTO> basketItems;
  public final float subTotal;
  public final float tax;
  public final float total;
    //this.message = Preconditions.checkNotNull(message, "message");

  @JsonCreator
  public BasketState(String userUuid, Set<ItemDTO> basketItems, float subTotal, float tax, float total) {
    this.userUuid = userUuid;
    this.basketItems = Preconditions.checkNotNull(basketItems,"items list/set is null");
    this.subTotal = subTotal;
    this.tax = tax;
    this.total = total;
  }

  public BasketState addOrUpdateItem(ItemDTO item) {
      Set<ItemDTO> newBasketItems = new HashSet<ItemDTO>();
      newBasketItems.addAll(basketItems);
      newBasketItems.add(item);
      log.severe("item values in add and remove items" + item.toString());
    return new BasketState(userUuid,newBasketItems,subTotal,tax,total);
  }
}
