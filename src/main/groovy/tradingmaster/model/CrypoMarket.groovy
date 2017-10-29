package tradingmaster.model


class CrypoMarket implements IMarket {

    String currency
    String asset
    String joinStr = '-'

    CrypoMarket(){
    }

    CrypoMarket(String currency, String asset) {
        this.currency = currency
        this.asset = asset
    }

    String getName() {
        return "${currency}${joinStr}${asset}"
    }

}
