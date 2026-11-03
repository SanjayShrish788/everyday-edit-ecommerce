package com.sanjay.Spring_ECOM.model.dto;

import java.util.List;

public record OrderRequest(String customerName,
                           String email,
                           List<OrderItemRequest> items)
{

}
