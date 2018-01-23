package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.stereotype.Service
import tradingmaster.db.entity.TradeBot
import tradingmaster.exchange.IExchangeAdapter
import tradingmaster.exchange.ExchangeResponse
import tradingmaster.model.BuySell
import tradingmaster.model.IOrder
import tradingmaster.model.ITicker
import tradingmaster.model.PriceRange

@Service
@Commons
class OrderExecutorService {

    ExchangeResponse<IOrder> placeLimitOrder(TradeBot bot,
                                             IExchangeAdapter exchangeAdapter,
                                             BuySell bs,
                                             BigDecimal spendOrAmount,
                                             PriceRange priceRange,
                                             String market) {

        ExchangeResponse<IOrder> res = trade(bot, exchangeAdapter, bs, spendOrAmount, priceRange, market, 1)

        return res
    }



    ExchangeResponse<IOrder> placeLimitOrder(TradeBot bot,
                                                     IExchangeAdapter exchangeAdapter,
                                                     BuySell bs,
                                                     BigDecimal spendOrAmount,
                                                     PriceRange priceRange,
                                                     String currency,
                                                     String asset) {

        String market = exchangeAdapter.buildMarket(currency, asset)

        ExchangeResponse<IOrder> res = trade(bot, exchangeAdapter, bs, spendOrAmount, priceRange, market, 1)

        return res
    }

    private ExchangeResponse<IOrder> trade(TradeBot bot,
                                   IExchangeAdapter exchangeAdapter,
                                   BuySell bs,
                                   BigDecimal spendOrAmount,
                                   PriceRange priceRange,
                                   String market, int tryCount) {

        ExchangeResponse<IOrder> orderResponse = new ExchangeResponse<IOrder>()

        try {

            log.info("($tryCount) Placing new ${bs.getName()} order for market $market. spendOrAmount: $spendOrAmount")

            ExchangeResponse<ITicker> tickerRes = exchangeAdapter.getTicker(market)

            if(!tickerRes.success) {
                String msg = "Can't proceed order! Ticker not loaded. ${tickerRes.message}"
                log.fatal(msg)
                orderResponse.success = false
                orderResponse.message = tickerRes.message
                return orderResponse
            }

            ITicker ticker = tickerRes.getResult()

            ExchangeResponse<String> orderIdRes = null

            if(bs == BuySell.BUY) {

                def quantity = spendOrAmount / ticker.getAsk()
                def price = ticker.getBid()

                // TODO: check allowed price range

                log.info("Attempting to BUY $market quantity: $quantity  price: $price")

                orderIdRes = exchangeAdapter.buyLimit(market, quantity, price)

            } else if (bs == BuySell.SELL) {

                def quantity = spendOrAmount
                def price = ticker.getAsk()

                // TODO: check allowed price range

                log.info("Attempting to SELL $market quantity: $quantity  price: $price")

                orderIdRes = exchangeAdapter.sellLimit(market, quantity, price)
            }

            if(!orderIdRes.success) {
                log.error("Attempting to trade $market was not sucsessful: $orderIdRes.message")
                orderResponse.success = false
                orderResponse.message = orderIdRes.message
                return orderResponse
            }

            String orderId = orderIdRes.getResult()

            log.info("Order with id $orderId has been placed. Waiting to be executed.....")

            Thread.sleep(60000) // Wait a minute than check

            IOrder order = checkOrderIfExecuted(bot, orderId, market, exchangeAdapter)

            if(order == null) {
                tryCount++
                log.info("Order $market is not executed.")
                trade(bot, exchangeAdapter, bs, spendOrAmount, priceRange, market, tryCount)
            }

            orderResponse.setResult(order)
            orderResponse.setSuccess(true)

        } catch (Exception e) {
            log.fatal("Error while placing order", e )
            orderResponse.success = false
            orderResponse.message = "Exception: ${e.getMessage()}"
            e.printStackTrace()
        }

        return orderResponse
    }


    IOrder checkOrderIfExecuted(TradeBot bot, String orderId, String market, IExchangeAdapter exchangeAdapter) {

        try {
            if(orderId == null) {
                log.fatal("OrderId for market id null!")
            }

            Thread.sleep(1000)

            ExchangeResponse<IOrder> orderRes = exchangeAdapter.getOrder(orderId)

            if(!orderRes.success) {
                log.error("Load order $market was not sucessful! $orderRes.message OrderId: $orderId")
                throw new RuntimeException("Load order $market was not sucessful! $orderRes.message OrderId: $orderId")
            }

            IOrder order = orderRes.getResult()
            log.info("Order $market received: $order")

            // Check remaining...!
            if(order.getQuantity().equals(order.getQuantityRemaining()) ) {
                log.info("Order $market has not been executed! Try to cancel order with id: $orderId")

                if(exchangeAdapter.cancelOrder(orderId)) {
                    log.info("Order $market has been canceld! Let's place a new order!")
                    return null
                } else {
                    log.info("Cancel process of order $market was not sucsesfull! Let's check again!")
                    return checkOrderIfExecuted(bot, orderId, market, exchangeAdapter)
                }
            } else if (order.getQuantityRemaining() > 0.0) {
                log.info("Order $market has a remaining quantity! Wair till order is fullfilled id: $orderId")
                return checkOrderIfExecuted(bot, orderId, market, exchangeAdapter)

            } else if (order.getQuantityRemaining() == 0.0) {

                return order
            } else {
                log.error("Unknown QuantityRemaining state! ${order.getQuantityRemaining()}")
                throw new RuntimeException("Unknown QuantityRemaining state! ${order.getQuantityRemaining()}")
            }

        } catch(Exception e) {
            log.error("Error while checking order $market id: $orderId", e)
            return checkOrderIfExecuted(bot, orderId, market, exchangeAdapter)
        }
    }

}
