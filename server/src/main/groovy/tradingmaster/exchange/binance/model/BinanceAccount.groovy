package tradingmaster.exchange.binance.model

class BinanceAccount {

    BigDecimal makerCommission

    BigDecimal takerCommission

    BigDecimal buyerCommission

    BigDecimal sellerCommission

    Boolean canTrade

    Boolean canWithdraw

    Boolean canDeposit

    Long updateTime

    List<BinanceBalance> balances







}
