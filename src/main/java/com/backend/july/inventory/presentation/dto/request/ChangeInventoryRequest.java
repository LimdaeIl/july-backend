package com.backend.july.inventory.presentation.dto.request;

import jakarta.validation.constraints.Positive;

public record ChangeInventoryRequest(
        @Positive(message = "수량은 0보다 커야 합니다.")
        int quantity
) {

}
