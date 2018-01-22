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

                log.info("Attempting to BUY: quantity: $quantity  price: $price")

                orderIdRes = exchangeAdapter.buyLimit(market, quantity, price)

            } else if (bs == BuySell.SELL) {

                def quantity = spendOrAmount
                def price = ticker.getAsk()

                // TODO: check allowed price range

                log.info("Attempting to SELL: quantity: $quantity  price: $price")

                orderIdRes = exchangeAdapter.sellLimit(market, quantity, price)
            }

            if(!orderIdRes.success) {
                orderResponse.success = false
                orderResponse.message = orderIdRes.message
                return orderResponse
            }

            String orderId = orderIdRes.getResult()

            Thread.sleep(60000) // Wait a minute
            ExchangeResponse<IOrder> orderRes = exchangeAdapter.getOrder(orderId)

            if(!orderRes.success) {
                log.info("Can't load order from Exchange. $orderRes.message OrderId: $orderId")
                orderResponse.success = false
                orderResponse.message = orderRes.message
                return orderResponse
            }

            IOrder order = orderRes.getResult()

            if(order.isOpen()) {
                log.info("Order is still open! Try to cancel order with id: $orderId")
                // order is still open -> cancel and retry
                if(exchangeAdapter.cancelOrder(orderId)) {
                    // next try
                    tryCount++
                    trade(bot, exchangeAdapter, bs, spendOrAmount, priceRange, market, tryCount )
                }
            }

            orderResponse.setResult(order)

        } catch (Exception e) {
            log.fatal("Error while placing order", e )
            orderResponse.success = false
            orderResponse.message = "Exception: ${e.getMessage()}"
            e.printStackTrace()
        }

        return orderResponse
    }

}
