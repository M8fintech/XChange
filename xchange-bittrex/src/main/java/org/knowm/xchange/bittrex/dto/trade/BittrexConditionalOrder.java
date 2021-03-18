package org.knowm.xchange.bittrex.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BittrexConditionalOrder {
  private String id;
  private String marketSymbol;
  private String operand;
  private String triggerPrice;
  private String trailingStopPercent;
  private String createdOrderId;
  private BittrexNewOrder orderToCreate;
  private OrderToCancel orderToCancel;
  private String clientConditionalOrderId;
  private String status;
  private String orderCreationErrorCode;
  private String createdAt;
  private String updatedAt;
  private String closedAt;
}
