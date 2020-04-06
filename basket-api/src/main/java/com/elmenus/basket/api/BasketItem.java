package com.elmenus.basket.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.util.UUID;

/**
 * An item in a user basket
 */
@Value
@JsonDeserialize
public final class BasketItem {


    public final String uuid;
    public final String quantity;
    public final String price;


   @JsonCreator
    public BasketItem(String uuid,  String quantity, String price) {
        this.uuid = Preconditions.checkNotNull(uuid, "Item UUID is missing");
        this.quantity = quantity;
        this.price = price;
    }


}
