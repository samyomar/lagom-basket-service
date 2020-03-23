package com.elmenus.basket.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

@Value
@JsonDeserialize
public class ItemDTO {

    public final String uuid;
    public final String quantity;
    public final String price;

    @JsonCreator
    public ItemDTO(String uuid, String quantity, String price) {
        this.uuid = uuid;
        this.quantity = quantity;
        this.price = price;
    }
}
