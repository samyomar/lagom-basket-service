package com.elmenus.basket.impl;

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
  public final List<ItemDTO> items;
  public final float subTotal;
  public final float tax ;
  public final float total;

  @JsonCreator
  public BasketState(String userUuid, List<ItemDTO> items, float subTotal, float tax, float total) {
    this.userUuid = userUuid;
    this.items = Preconditions.checkNotNull(items,"items list/set is null");
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
              if (items.get(i).itemUuid.equals(newItem.itemUuid)) {
                  items.set(i, newItem);
                  return;
              }
          }
      }
      items.add(newItem);
      return ;
  }


  // check if this basket state already contained this userID or not.
    // it will return true if the state object already has another userID assigned.
  public boolean hasAnotherUser(String userUuid)
  {
      return (this.userUuid!=null && !this.userUuid.trim().isEmpty() && !this.userUuid.equals(userUuid));
  }

  public BasketState addOrUpdateItem(String userUuid, String itemUuid, int quantity, float price,float taxPercentage) {

      // get all the event meta data here including the updated tax.
      // calculate everything and return new status with the updated final calculations
      List<ItemDTO> newItems = new ArrayList<ItemDTO>();
      newItems.addAll(items);
      updateItemsList(newItems , new ItemDTO(itemUuid,quantity,price));

      // It will be faster to calculate only the delta change using the new item QTY and Price,
      // but it will be more generic and maintainable to calculate it based on the items set, as this same calculations will be user later from removeItem or any similar function.
      float newSubTotal = calculateSubTotal(newItems);
      float newTax = calculateTaxValue(newSubTotal,taxPercentage);
      float newTotal = calculateTotal(newSubTotal , newTax);

    return new BasketState(userUuid,newItems,newSubTotal,newTax,newTotal);
  }

  private float calculateSubTotal(List<ItemDTO> newItems)
  {
      float newSubTotal = 0;
      for(ItemDTO item:newItems)
      {
          newSubTotal +=  item.price *  item.quantity;
      }
       return newSubTotal;
  }

  private float calculateTaxValue(float newSubTotal , float newTaxPercentage)
  {
        return newSubTotal *  (newTaxPercentage/100);
  }

  private float calculateTotal(float newSubTotal , float newTax)
  {
      return newSubTotal + newTax;
  }



}
