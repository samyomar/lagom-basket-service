package com.elmenus.basket.impl;

import lombok.Value;

@Value
public class ItemDTO {

    public final String itemUuid;
    public final int quantity;
    public final float price;

    public ItemDTO(String itemUuid, int quantity, float price) {
        this.itemUuid = itemUuid;
        this.quantity = quantity;
        this.price = price;
    }


}
