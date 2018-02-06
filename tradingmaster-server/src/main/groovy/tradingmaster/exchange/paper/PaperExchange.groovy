package tradingmaster.exchange.paper

import tradingmaster.exchange.DefaultExchageAdapter
import tradingmaster.exchange.bittrex.model.BittrexBalance
import tradingmaster.exchange.ExchangeResponse
import tradingmaster.model.*

class PaperExchange extends DefaultExchageAdapter {

    Map config

    PaperExchange() {
        super("PaperExchange")
    }

    @Override
    Boolean cancelOrder(String market, String id) {
        return null
    }

    @Override
    ExchangeResponse<String> sellLimit(String market, BigDecimal quantity, BigDecimal rate) {
        return null
    }

    @Override
    ExchangeResponse<String> buyLimit(String market, BigDecimal quantity, BigDecimal rate) {
        return null
    }

    @Override
    ExchangeResponse<ITicker> getTicker(String market) {
        return null
    }

    @Override
    ExchangeResponse<IOrder> getOrder(String market, String id) {
        return null
    }

    @Override
    List<IOrder> getOrderHistory() {
        return null
    }

    TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market) {
        return null
    }

    @Override
    List<CryptoMarket> getMakets() {
        return null
    }

    List<IBalance> getBalances() {
        def balances = []

        IBalance b = new BittrexBalance()
        b.currency = config.baseCurrency
        b.value = config.startBalance

        balances << b

        return balances
    }




}
