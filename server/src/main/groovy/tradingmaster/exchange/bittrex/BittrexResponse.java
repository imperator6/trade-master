package tradingmaster.exchange.bittrex;


import com.fasterxml.jackson.annotation.JsonProperty;
import tradingmaster.exchange.bittrex.model.BittrexTrade;

import java.util.List;

public class BittrexResponse {

    @JsonProperty
    boolean success;

    @JsonProperty
    String message;

    @JsonProperty
    List<BittrexTrade> result;

}
