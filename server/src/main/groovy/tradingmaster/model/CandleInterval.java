package tradingmaster.model;

public enum CandleInterval {

    ONE_MINUTE("1m", 1),
    FIVE_MINUTES("5m", 5),
    FIFTEEN_MINUTES("15m", 15),
    THIRTY_MINUTES("30m", 30),
    ONE_HOUR("1h", 60),
    TWO_HOURS("2h", 120),
    FOUR_HOURS("4h", 240),
    ONE_DAY("4h", 24*60),
    THREE_DAYS("3d", 24*60*3);

    String key;
    Integer minuteValue;

    CandleInterval(String key, Integer minValue) {
        this.key = key;
        this.minuteValue = minValue;
    }

    public String getKey() {
        return key;
    }

    public Integer getMinuteValue() {
        return minuteValue;
    }

    static CandleInterval parse(final String searchKey) {

        for(CandleInterval i:CandleInterval.values()) {
            if(searchKey.toLowerCase().equals(i.getKey())) {
                return i;
            }
        }

        return null;
    }

}
