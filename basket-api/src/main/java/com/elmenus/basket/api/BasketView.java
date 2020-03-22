package com.elmenus.basket.api;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;
import java.util.List;
import java.util.UUID;

@Value
@JsonDeserialize
public final class BasketView {

    public final String uuid;
    public final String userUuid;
    public final List<ItemDTO> basketItems;
    public final float subTotal;
    public final float tax;
    public final float total;

    @JsonCreator
    public BasketView(String uuid, String userUuid, List<ItemDTO> basketItems, float subTotal, float total, float tax) {
        this.uuid = Preconditions.checkNotNull(uuid, "uuid");
        this.userUuid = Preconditions.checkNotNull(userUuid, "userUuid");
        this.basketItems = Preconditions.checkNotNull(basketItems, "items");
        this.subTotal = subTotal;
        this.tax = tax;
        this.total = total;
    }

    public boolean hasItem(String itemId) {
        return basketItems.stream().anyMatch(basketItem -> basketItem.getItemId().equals(itemId));
    }


}
