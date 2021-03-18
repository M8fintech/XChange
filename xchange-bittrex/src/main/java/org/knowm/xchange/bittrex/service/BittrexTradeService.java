package org.knowm.xchange.bittrex.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.knowm.xchange.bittrex.*;
import org.knowm.xchange.bittrex.dto.BittrexException;
import org.knowm.xchange.bittrex.dto.trade.BittrexOrder;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.*;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;

public class BittrexTradeService extends BittrexTradeServiceRaw implements TradeService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public BittrexTradeService(
      BittrexExchange exchange,
      BittrexAuthenticated bittrex,
      ResilienceRegistries resilienceRegistries) {
    super(exchange, bittrex, resilienceRegistries);
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {
    try {
      return placeBittrexLimitOrder(limitOrder);
    } catch (BittrexException e) {
      throw BittrexErrorAdapter.adapt(e);
    }
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    return getOpenOrders(createOpenOrdersParams());
  }

  @Override
  public OpenOrders getOpenOrders(OpenOrdersParams params) throws IOException {
    try {
      return new OpenOrders(BittrexAdapters.adaptOpenOrders(getBittrexOpenOrders(params)));
    } catch (BittrexException e) {
      throw BittrexErrorAdapter.adapt(e);
    }
  }

  @Override
  public boolean cancelOrder(String orderId) throws IOException {
    try {
      return BittrexConstants.CLOSED.equalsIgnoreCase(cancelBittrexLimitOrder(orderId).getStatus());
    } catch (BittrexException e) {
      throw BittrexErrorAdapter.adapt(e);
    }
  }

  @Override
  public boolean cancelOrder(CancelOrderParams orderParams) throws IOException {
    if (orderParams instanceof CancelOrderByIdParams) {
      return cancelOrder(((CancelOrderByIdParams) orderParams).getOrderId());
    }
    return false;
  }

  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
    try {
      List<BittrexOrder> tradeHistory =
          (params instanceof TradeHistoryParamCurrencyPair)
              ? getBittrexUserTradeHistory(
                  ((TradeHistoryParamCurrencyPair) params).getCurrencyPair())
              : getBittrexUserTradeHistory();
      return new UserTrades(
          BittrexAdapters.adaptUserTrades(tradeHistory), Trades.TradeSortType.SortByTimestamp);
    } catch (BittrexException e) {
      throw BittrexErrorAdapter.adapt(e);
    }
  }

  @Override
  public TradeHistoryParams createTradeHistoryParams() {
    return new DefaultTradeHistoryParamCurrencyPair();
  }

  @Override
  public OpenOrdersParams createOpenOrdersParams() {
    return new DefaultOpenOrdersParamCurrencyPair();
  }

  @Override
  public Collection<Order> getOrder(String... orderIds) throws IOException {
    try {
      List<Order> orders = new ArrayList<>();
      for (String orderId : orderIds) {
        BittrexOrder order = getBittrexOrder(orderId);
        if (order != null) {
          LimitOrder limitOrder = BittrexAdapters.adaptOrder(order);
          orders.add(limitOrder);
        }
      }
      return orders;
    } catch (BittrexException e) {
      throw BittrexErrorAdapter.adapt(e);
    }
  }

  /**
   * Place an OCO order
   *
   * @param limitOrderId id of already sent limit oder
   * @param stopOrder stop order
   * @return the list order ID
   * @throws ExchangeException - Indication that the exchange reported some kind of error with the
   *     request or response
   * @throws NotAvailableFromExchangeException - Indication that the exchange does not support the
   *     requested function or data
   * @throws NotYetImplementedForExchangeException - Indication that the exchange supports the
   *     requested function or data, but it has not yet been implemented
   * @throws IOException - Indication that a networking error occurred while fetching JSON data
   * @see org.knowm.xchange.utils.OrderValuesHelper
   */
  public String placeOcoOrder(String limitOrderId, StopOrder stopOrder) throws IOException {
    try {
      return placeBittrexOcoOrder(limitOrderId, stopOrder);
    } catch (BittrexException e) {
      throw BittrexErrorAdapter.adapt(e);
    }
  }
}
