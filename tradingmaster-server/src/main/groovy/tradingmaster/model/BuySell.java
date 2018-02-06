package tradingmaster.model;

public enum BuySell {

    BUY("buy"), SELL("sell");

    String name;

    BuySell(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
