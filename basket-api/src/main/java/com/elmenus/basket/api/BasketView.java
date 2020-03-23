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
    public final List<ItemDTO> items;
    public final String subTotal;
    public final String tax;
    public final String total;

    @JsonCreator
    public BasketView(String uuid, String userUuid, List<ItemDTO> items, String subTotal, String tax , String total) {
        this.uuid = Preconditions.checkNotNull(uuid, "uuid");
        this.userUuid = Preconditions.checkNotNull(userUuid, "userUuid");
        this.items = Preconditions.checkNotNull(items, "items");
        this.subTotal = subTotal;
        this.tax = tax;
        this.total = total;
    }

    public boolean hasItem(String itemId) {
        return items.stream().anyMatch(basketItem -> basketItem.uuid.equals(itemId));
    }


}
