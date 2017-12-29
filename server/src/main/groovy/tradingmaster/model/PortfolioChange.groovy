package tradingmaster.model

class PortfolioChange extends TradingSignal {

    BigDecimal fee

    BigDecimal assetOld
    BigDecimal assetNew

    BigDecimal currencyOld
    BigDecimal currencyNew

    BigDecimal balanceOld
    BigDecimal balanceNew

    Integer tradeNumber

}
