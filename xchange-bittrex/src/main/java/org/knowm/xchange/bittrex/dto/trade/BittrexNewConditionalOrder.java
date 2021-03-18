package org.knowm.xchange.bittrex.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BittrexNewConditionalOrder {
  private String marketSymbol;
  private String operand;
  private String triggerPrice;
  private String trailingStopPercent;
  private BittrexNewOrder orderToCreate;
  private OrderToCancel orderToCancel;
  private String clientConditionalOrderId;
}
