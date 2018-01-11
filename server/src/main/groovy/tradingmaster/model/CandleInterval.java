package tradingmaster.model;

public enum CandleInterval {

    ONE_MINUTE("1m"), FIVE_MINUTES("5m");

    String key;

    CandleInterval(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
