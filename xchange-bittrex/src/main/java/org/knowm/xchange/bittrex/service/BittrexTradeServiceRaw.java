package org.knowm.xchange.bittrex.service;

import static org.knowm.xchange.bittrex.BittrexResilience.GET_CLOSED_ORDERS_RATE_LIMITER;

import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.bittrex.*;
import org.knowm.xchange.bittrex.dto.batch.BatchResponse;
import org.knowm.xchange.bittrex.dto.batch.order.BatchOrder;
import org.knowm.xchange.bittrex.dto.batch.order.neworder.TimeInForce;
import org.knowm.xchange.bittrex.dto.trade.*;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;

@Slf4j
public class BittrexTradeServiceRaw extends BittrexBaseService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public BittrexTradeServiceRaw(
      BittrexExchange exchange,
      BittrexAuthenticated bittrex,
      ResilienceRegistries resilienceRegistries) {
    super(exchange, bittrex, resilienceRegistries);
  }

  public String placeBittrexLimitOrder(LimitOrder limitOrder) throws IOException {
    return placeBittrexLimitOrder(limitOrder, TimeInForce.GOOD_TIL_CANCELLED);
  }

  public String placeBittrexLimitOrder(LimitOrder limitOrder, TimeInForce type) throws IOException {
    BittrexNewOrder bittrexNewOrder =
        new BittrexNewOrder(
            BittrexUtils.toPairString(limitOrder.getCurrencyPair()),
            OrderType.BID.equals(limitOrder.getType())
                ? BittrexConstants.BUY
                : BittrexConstants.SELL,
            BittrexConstants.LIMIT,
            limitOrder.getRemainingAmount().toPlainString(),
            null,
            limitOrder.getLimitPrice().toPlainString(),
            type.toString(),
            null,
            null);
    return bittrexAuthenticated
        .placeOrder(
            apiKey, System.currentTimeMillis(), contentCreator, signatureCreator, bittrexNewOrder)
        .getId();
  }

  public String placeBittrexConditionalOrder(BittrexNewConditionalOrder conditionalOrder)
      throws IOException {
    return bittrexAuthenticated
        .placeConditionalOrder(
            apiKey, System.currentTimeMillis(), contentCreator, signatureCreator, conditionalOrder)
        .getId();
  }

  public BittrexConditionalOrder cancelBittrexConditionalOrder(String conditionalOrderId)
      throws IOException {
    return bittrexAuthenticated.cancelConditionalOrder(
        apiKey, System.currentTimeMillis(), contentCreator, signatureCreator, conditionalOrderId);
  }

  public BittrexOrder cancelBittrexLimitOrder(String orderId) throws IOException {
    return bittrexAuthenticated.cancelOrder(
        apiKey, System.currentTimeMillis(), contentCreator, signatureCreator, orderId);
  }

  public List<BittrexOrder> getBittrexOpenOrders(OpenOrdersParams params) throws IOException {
    return bittrexAuthenticated.getOpenOrders(
        apiKey, System.currentTimeMillis(), contentCreator, signatureCreator);
  }

  public SequencedOpenOrders getBittrexSequencedOpenOrders(OpenOrdersParams params)
      throws IOException {
    BittrexOrders openOrders =
        bittrexAuthenticated.getOpenOrders(
            apiKey, System.currentTimeMillis(), contentCreator, signatureCreator);
    return new SequencedOpenOrders(
        openOrders.getSequence(), BittrexAdapters.adaptOpenOrders(openOrders));
  }

  public List<BittrexOrder> getBittrexUserTradeHistory(CurrencyPair currencyPair)
      throws IOException {
    return decorateApiCall(
            () ->
                bittrexAuthenticated.getClosedOrders(
                    apiKey,
                    System.currentTimeMillis(),
                    contentCreator,
                    signatureCreator,
                    BittrexUtils.toPairString(currencyPair),
                    200))
        .withRetry(retry("getClosedOrders"))
        .withRateLimiter(rateLimiter(GET_CLOSED_ORDERS_RATE_LIMITER))
        .call();
  }

  public List<BittrexOrder> getBittrexUserTradeHistory() throws IOException {
    return getBittrexUserTradeHistory(null);
  }

  public BittrexOrder getBittrexOrder(String orderId) throws IOException {
    return bittrexAuthenticated.getOrder(
        apiKey, System.currentTimeMillis(), contentCreator, signatureCreator, orderId);
  }

  public BatchResponse[] executeOrdersBatch(BatchOrder[] batchOrders) throws IOException {
    return bittrexAuthenticated.executeOrdersBatch(
        apiKey, System.currentTimeMillis(), contentCreator, signatureCreator, batchOrders);
  }

  @AllArgsConstructor
  @Getter
  public static class SequencedOpenOrders {
    private final String sequence;
    private final List<LimitOrder> openOrders;
  }
}
