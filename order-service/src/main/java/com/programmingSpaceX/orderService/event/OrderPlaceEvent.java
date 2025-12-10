package com.programmingSpaceX.orderService.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlaceEvent {
    private String orderNumber;
}
