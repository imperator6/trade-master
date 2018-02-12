package tradingmaster.service

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tradingmaster.db.entity.TradeBot
import tradingmaster.exchange.IExchangeAdapter
import tradingmaster.exchange.ExchangeResponse
import tradingmaster.model.BuySell
import tradingmaster.model.IOrder
import tradingmaster.model.ITicker
import tradingmaster.model.PriceLimit

@Service
@Commons
class OrderExecutorService {

    @Autowired
    PushoverService pushoverService

    ExchangeResponse<IOrder> placeLimitOrder(TradeBot bot,
                                             IExchangeAdapter exchangeAdapter,
                                             BuySell bs,
                                             BigDecimal spendOrAmount,
                                             PriceLimit priceRange,
                                             String market) {

        ExchangeResponse<IOrder> res = trade(bot, exchangeAdapter, bs, spendOrAmount, priceRange, market, 1)

        return res
    }



    ExchangeResponse<IOrder> placeLimitOrder(TradeBot bot,
                                             IExchangeAdapter exchangeAdapter,
                                             BuySell bs,
                                             BigDecimal spendOrAmount,
                                             PriceLimit priceRange,
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
                                           PriceLimit priceLimit,
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
                def price = ticker.getBid() // use aks instead to sell son as possible?

                if(priceLimit != null) {
                    log.info("BUY: Checking price limit: $priceLimit")
                    if(price > priceLimit.getPriceLimit()) {
                        String msg = "Price to HIGH! Signal: ${priceLimit.signalPrice} Current: ${price} > ${priceLimit.priceLimitPercent}% diff"
                        log.warn(msg)
                        orderResponse.success = false
                        orderResponse.message = msg
                        return orderResponse
                    }
                }

                log.info("Attempting to BUY $market quantity: $quantity  price: $price")

                orderIdRes = exchangeAdapter.buyLimit(market, quantity, price)

            } else if (bs == BuySell.SELL) {

                def quantity = spendOrAmount
                def price = ticker.getBid() // use the current bid to sell soon as possible

                if(priceLimit != null) {
                    log.info("SELL: Checking price limit: $priceLimit")
                    if(price < priceLimit.getPriceLimit()) {
                        String msg = "Price to LOW! Signal: ${priceLimit.signalPrice} Current: ${price} > ${priceLimit.priceLimitPercent}% diff"
                        log.warn(msg)
                        orderResponse.success = false
                        orderResponse.message = msg
                        return orderResponse
                    }
                }

                log.info("Attempting to SELL $market quantity: $quantity  price: $price")

                orderIdRes = exchangeAdapter.sellLimit(market, quantity, price)
            }

            if(!orderIdRes.success) {

                String msg = "Attempting to trade $market was not sucsessful: $orderIdRes.message"
                pushoverService.send(bot, "Trade Error $market", msg)
                log.error(msg)
                orderResponse.success = false
                orderResponse.message = orderIdRes.message
                return orderResponse
            }

            String orderId = orderIdRes.getResult()

            log.info("Order with id $orderId has been placed. Waiting to be executed.....")

            Thread.sleep(60000) // Wait a minute than check

            IOrder order = checkOrderIfExecuted(bot, orderId, market, exchangeAdapter, 1)

            if(order == null) {
                tryCount++
                log.info("Order $market is not executed.")
                return trade(bot, exchangeAdapter, bs, spendOrAmount, priceLimit, market, tryCount)
            }

            orderResponse.setResult(order)
            orderResponse.setSuccess(true)

            String msg = "quantity: ${order.quantity} price: ${order.pricePerUnit}"
            pushoverService.send(bot, "$bs $market", msg)

        } catch (Exception e) {
            log.fatal("Error while placing order", e )
            orderResponse.success = false
            orderResponse.message = "Error while placing order ${e.getMessage()}"

            pushoverService.send(bot, "Trade Error $market", orderResponse.message)
            e.printStackTrace()
        }

        return orderResponse
    }


    IOrder checkOrderIfExecuted(TradeBot bot, String orderId, String market, IExchangeAdapter exchangeAdapter, Integer count) {

        try {
            if(orderId == null) {
                log.fatal("OrderId for market id null!")
            }

            Thread.sleep(1000)
            count++

            if(count > 2) {
                Thread.sleep(count * 10000)
            }

            ExchangeResponse<IOrder> orderRes = exchangeAdapter.getOrder(market, orderId)

            if(!orderRes.success) {
                log.error("Load order $market was not sucessful! $orderRes.message OrderId: $orderId")
                throw new RuntimeException("Load order $market was not sucessful! $orderRes.message OrderId: $orderId")
            }

            IOrder order = orderRes.getResult()
            log.info("Order $market received: $order")

            // Check remaining...!
            if(order.getQuantity().equals(order.getQuantityRemaining()) ) {
                log.info("Order $market has not been executed! Try to cancel order with id: $orderId")

                if(exchangeAdapter.cancelOrder(market, orderId)) {
                    log.info("Order $market has been canceld! Let's place a new order!")
                    return null
                } else {
                    log.info("Cancel process of order $market was not sucsesfull! Let's check again!")
                    return checkOrderIfExecuted(bot, orderId, market, exchangeAdapter, count)
                }
            } else if (order.getQuantityRemaining() > 0.0) {

                if(count > 30) {
                    // We need cancel the order ....
                    if(exchangeAdapter.cancelOrder(market, orderId)) {
                        log.info("Order $market has been canceld with remaining quantity ${order.getQuantityRemaining()}")
                        return order
                    }
                }

                log.info("Order $market has a remaining quantity! Wait till order is fullfilled id: $orderId")
                return checkOrderIfExecuted(bot, orderId, market, exchangeAdapter, count)

            } else if (order.getQuantityRemaining() == 0.0) {

                return order
            } else {
                log.error("Unknown QuantityRemaining state! ${order.getQuantityRemaining()}")
                throw new RuntimeException("Unknown QuantityRemaining state! ${order.getQuantityRemaining()}")
            }

        } catch(Exception e) {
            log.error("Error while checking order $market id: $orderId", e)
            return checkOrderIfExecuted(bot, orderId, market, exchangeAdapter, count)
        }
    }

}
