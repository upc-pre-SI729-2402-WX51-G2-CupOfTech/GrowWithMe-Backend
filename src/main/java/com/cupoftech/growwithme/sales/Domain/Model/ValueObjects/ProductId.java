package com.cupoftech.growwithme.sales.Domain.Model.ValueObjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ProductId(Long productId) {
    public ProductId(){
        this(0L);
    }
    public ProductId {
        if (productId == null || productId < 0) {
            throw new IllegalArgumentException("Product Id cannot be null or negative");
        }

    }
}
