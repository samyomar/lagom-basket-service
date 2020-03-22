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

    public final UUID itemId;
    public final UUID userUuid;
    public final int quantity;
    public final float price;

    @JsonCreator
    public BasketItem(UUID itemId, UUID userUuid,  int quantity, float price) {
        this.itemId = Preconditions.checkNotNull(itemId, "Item id is missing");
        this.userUuid=Preconditions.checkNotNull(userUuid,"User ID is empty");
        this.quantity = quantity;
        this.price = price;
    }

}
