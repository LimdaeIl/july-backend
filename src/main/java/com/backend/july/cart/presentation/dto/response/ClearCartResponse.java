package com.backend.july.cart.presentation.dto.response;

public record ClearCartResponse(
        int deletedItemCount
) {

    public static ClearCartResponse of(int deletedItemCount) {
        return new ClearCartResponse(deletedItemCount);
    }
}
