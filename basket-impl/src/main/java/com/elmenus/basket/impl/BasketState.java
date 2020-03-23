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
 * The state for the {@link BasketAggregate} aggregate.
 */
@SuppressWarnings("serial")
@Value
@JsonDeserialize
@Log
public final class BasketState implements CompressedJsonable {

  public static final BasketState EMPTY = new BasketState("", Collections.emptyList(), 0,0,0);

 // public static final BasketState INITIAL = new BasketState("Hello", LocalDateTime.now().toString());
  public final String userUuid;
  public final List<ItemDTO> basketItems;
  public final float subTotal;
  public final float tax;
  public final float total;

  @JsonCreator
  public BasketState(String userUuid, List<ItemDTO> basketItems, float subTotal, float tax, float total) {
    this.userUuid = userUuid;
    this.basketItems = Preconditions.checkNotNull(basketItems,"items list/set is null");
    this.subTotal = subTotal;
    this.tax = tax;
    this.total = total;
  }

  // this function check if the item is already exist in the list, it just update it.
  // else it add it to the list.
  private void updateItemsList(List<ItemDTO> items, ItemDTO newItem)
  {
      if(!items.isEmpty()) {
          for (int i = 0; i < items.size(); i++) {
              if (items.get(i).getUuid().equals(newItem.getUuid())) {
                  items.set(i, newItem);
                  return;
              }
          }
      }
      items.add(newItem);
      return ;
  }

  public BasketState addOrUpdateItem(String userUuid, String itemUuid, int quantity, float price,float tax) {

      // get all the event meta data here including the updated tax.
      // calculate everything and return new status with the updated final calculations
      log.severe("I came here with basketItems" + basketItems );
      List<ItemDTO> newBasketItems = new ArrayList<ItemDTO>();
      newBasketItems.addAll(basketItems);
      //List<ItemDTO> newBasketItems =
      updateItemsList(newBasketItems , new ItemDTO(itemUuid,quantity+"",price+""));

      log.severe("after the loop with list size = " + newBasketItems.size());
      // It will be faster to calculate only the delta change using the new item QTY and Price,
      // but it will be more generic and maintainable to calculate it based on the items set, as this same calculations will be user later from removeItem or any similar function.
      float newSubTotal = calculateSubTotal(newBasketItems);
      float newTotal = calculateTotal(newSubTotal , tax);

     // log.severe("item values in add and remove items" + item.toString());
    return new BasketState(userUuid,newBasketItems,newSubTotal,tax,newTotal);
  }

  private float calculateSubTotal(List<ItemDTO> newBasketItems)
  {
      float newSubTotal = 0;
      for(ItemDTO item:newBasketItems)
      {
          newSubTotal += Float.parseFloat(item.price) * Integer.parseInt(item.quantity);
      }
       return newSubTotal;
     // basketItems.stream().forEach( (item) -> {subTotal2 = Float.parseFloat(item.price) * Integer.parseInt(item.price);});
  }

  private float calculateTotal(float newSubTotal , float newTax)
  {
      return newSubTotal + newTax ;
  }

}
