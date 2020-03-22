package com.elmenus.basket.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

@Value
@JsonDeserialize
public class ItemDTO {

    public final String itemId;
    public final int quantity;
    public final float price;

    @JsonCreator
    public ItemDTO(String itemId, int quantity, float price) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.price = price;
    }
}
