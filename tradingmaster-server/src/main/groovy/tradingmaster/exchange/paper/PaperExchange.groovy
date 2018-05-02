package tradingmaster.exchange.paper

import tradingmaster.db.entity.json.Config
import tradingmaster.exchange.DefaultExchageAdapter
import tradingmaster.exchange.ExchangeResponse
import tradingmaster.exchange.bittrex.model.BittrexBalance
import tradingmaster.model.*

import java.util.concurrent.atomic.AtomicInteger

class PaperExchange extends DefaultExchageAdapter {

    static AtomicInteger nextOrderId = new AtomicInteger(0)

    Config config

    Map<String, IBalance> balances = [:]

    Map<String, ITicker> tickerMap = [:]

    Candle candle

    Map orderMap = [:]


    PaperExchange() {
        super("PaperExchange")
    }

    synchronized void setTicker(BigDecimal signalPrice, String currency, String asset) {

        Ticker t = new Ticker()

        String makret =  buildMarket(currency, asset)
        t.setMarket( makret )
        t.setBid(signalPrice)
        t.setAsk(signalPrice)

        this.tickerMap.put(makret, t)
    }


    @Override
    synchronized Boolean cancelOrder(String market, String id) {
        return null
    }

    @Override
    synchronized ExchangeResponse<String> sellLimit(String market, BigDecimal quantity, BigDecimal rate) {

        int nextOrderId = nextOrderId.incrementAndGet()

        Order order = new Order()
        order.id = nextOrderId
        order.market = market
        order.quantity = quantity
        order.quantityRemaining = 0.0
        order.pricePerUnit = rate
        order.price = quantity * rate
        order.buySell = "sell"
        order.open = false
        order.closeDate = candle.end
        order.timeStamp = candle.end

        def fee = quantity * rate * config.backtest.fee
        order.commissionPaid = fee

        String nextOrderIdKey = "" + nextOrderId

        CryptoMarket cm = new CryptoMarket(this.name, market)
        addBalance( cm.getCurrency(), order.price - order.commissionPaid)

        this.orderMap.put(nextOrderIdKey, order)
        return new ExchangeResponse<String>(nextOrderIdKey)
    }

    @Override
    synchronized ExchangeResponse<String> buyLimit(String market, BigDecimal quantity, BigDecimal rate) {

        int nextOrderId = nextOrderId.incrementAndGet()

        Order order = new Order()
        order.id = nextOrderId
        order.market = market
        order.quantity = quantity
        order.quantityRemaining = 0.0
        order.pricePerUnit = rate
        order.price = quantity * rate
        order.buySell = "buy"
        order.open = false
        order.closeDate = candle.end
        order.timeStamp = candle.end

        String nextOrderIdKey = "" + nextOrderId
        order.commissionPaid =  quantity * rate * config.backtest.fee

        CryptoMarket cm = new CryptoMarket(this.name, market)

        addBalance( cm.getCurrency(), (order.price * -1) - order.commissionPaid)

        this.orderMap.put(nextOrderIdKey, order)
        return new ExchangeResponse<String>(nextOrderIdKey)
    }

    @Override
    synchronized ExchangeResponse<ITicker> getTicker(String market) {
        ITicker ticker = tickerMap.get(market)
        return new ExchangeResponse<ITicker>(ticker)
    }

    @Override
    synchronized ExchangeResponse<IOrder> getOrder(String market, String id) {
        return new ExchangeResponse<IOrder>( this.orderMap.get(id))
    }

    @Override
    synchronized List<IOrder> getOrderHistory() {
        return new ArrayList(this.orderMap.values())
    }

    synchronized TradeBatch getTrades(Date startDate, Date endDate, CryptoMarket market) {
        return null
    }

    @Override
    synchronized List<CryptoMarket> getMakets() {
        return null
    }

    synchronized List<IBalance> getBalances() {
        return new ArrayList<IBalance>(balances.values())
    }

    synchronized void setBalance(String currency, BigDecimal value) {

        IBalance b = new BittrexBalance()
        b.currency = currency
        b.value = value

        this.balances.put(currency, b)
    }

    synchronized void addBalance( String currency, BigDecimal value) {

        def balance = getBalance(currency).value

        setBalance( currency, (balance + value))
    }




}
