package tradingmaster.exchange.binance;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tradingmaster.exchange.DefaultExchangeRestService;
import tradingmaster.exchange.bittrex.EncryptionUtility;

import java.util.Map;


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

    boolean applySecutity(String resourcePath) {
        String rl = resourcePath.toLowerCase();
        if(rl.indexOf("order") > -1  || rl.indexOf("account") > -1) {
            return true;
        }

        return false;
    }

    @Override
    public Map addAdditionalParmas(String resourcePath, Map params) {

        if(applySecutity(resourcePath)) {
            params.put("timestamp", System.currentTimeMillis());
            params.put("recvWindow", 6_000_000L);
        }

        return params;
    }

    @Override
    protected String buildQueryString(String resourcePath, Map<String,?> params) {

       String url = super.buildQueryString(resourcePath, params);

        if(applySecutity(resourcePath)) {

            String query = "";
            boolean first = true;

            for(String key: params.keySet()){
                Object value= params.get(key);

                if(first) {
                    query += key + "=" + value;
                    first = false;
                } else {
                    query += "&" + key + "=" + value;
                }
            }

            String signature = EncryptionUtility.calculateHash(secret, query, "HmacSHA256");

            if(params.isEmpty()) {
                url += "?signature=" + signature;
            } else {
                url += "&signature=" + signature;
            }
        }

        return url;
    }

    @Override
    public HttpEntity<String> securityHeaders(String uri, String resourcePath, String method, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();

        //String timestamp = Instant.now().getEpochSecond() + "";
        String resource = uri.toString().replace(getBaseUrl(), "");

        String query = "";
        int queryStartIndex = resource.indexOf("?");
        if(queryStartIndex > -1) {
            query = resource.substring(queryStartIndex+1);
            //jsonBody = query;
        }

        if(applySecutity(resourcePath)) {
            headers.add("X-MBX-APIKEY", this.publicKey);
        }

        //request.addHeader("apisign", EncryptionUtility.calculateHash(secret, url, encryptionAlgorithm)); // Attaches signature as a header
        headers.add("accept", "application/json");
        headers.add("content-type", "application/json");

        curlRequest(method, jsonBody, headers, resource);

        return new HttpEntity<>(null, headers);
    }
}
