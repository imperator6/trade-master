package tradingmaster.exchange.bittrex;


import com.fasterxml.jackson.annotation.JsonProperty;
import tradingmaster.model.CryptoTrade;

import java.util.List;

public class BittrexResponse {

    @JsonProperty
    boolean success;

    @JsonProperty
    String message;

    @JsonProperty
    List<CryptoTrade> result;

}
