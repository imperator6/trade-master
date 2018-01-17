package tradingmaster.exchange.binance;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tradingmaster.exchange.DefaultExchangeRestService;

import java.time.Instant;


/**
 * Created by irufus on 2/25/15.
 */
@Component
public class BinanceExchangeImpl extends DefaultExchangeRestService {

    static Logger log = Logger.getLogger(BinanceExchangeImpl.class.getName());


    @Autowired
    public BinanceExchangeImpl(@Value("${binance.key}") String publicKey,
                               @Value("${binance.secret}") String secretKey,
                               @Value("${binance.api.baseUrl}") String baseUrl,
                               RestTemplate restTemplate) {

        super(publicKey, secretKey, null, baseUrl, restTemplate);
    }



    @Override
    public HttpEntity<String> securityHeaders(String uri, String endpoint, String method, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();

        String timestamp = Instant.now().getEpochSecond() + "";
        String resource = endpoint.replace(getBaseUrl(), "");

//        headers.add("accept", "application/json");
//        headers.add("content-type", "application/json");
//        headers.add("CB-ACCESS-KEY", publicKey);
//        headers.add("CB-ACCESS-SIGN", signature.generate(resource, method, jsonBody, timestamp));
//        headers.add("CB-ACCESS-TIMESTAMP", timestamp);
//        headers.add("CB-ACCESS-PASSPHRASE", passphrase);

        curlRequest(method, jsonBody, headers, resource);

        return new HttpEntity<>(jsonBody, headers);
    }
}
