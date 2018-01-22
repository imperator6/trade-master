package tradingmaster.exchange.bittrex;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tradingmaster.exchange.DefaultExchangeRestService;

import java.util.Map;


/**
 * Created by irufus on 2/25/15.
 */
@Component
public class BittrexExchangeImpl extends DefaultExchangeRestService {

    static Logger log = Logger.getLogger(BittrexExchangeImpl.class.getName());


    @Autowired
    public BittrexExchangeImpl(@Value("${bittrex.key}") String publicKey,
                               @Value("${bittrex.secret}") String secret,
                               @Value("${bittrex.api.baseUrl}") String baseUrl,
                               RestTemplate restTemplate) {

        super(publicKey, secret, null, baseUrl, restTemplate);
    }

    @Override
    public Map addAdditionalParmas(String resourcePath, Map params) {

        if(!(resourcePath.indexOf("public") > -1)) {
            params.put("apikey", publicKey);
            params.put("nonce", EncryptionUtility.generateNonce());
        }
        return params;
    }



    @Override
    public HttpEntity<String> securityHeaders(String uri, String resourcePath, String method, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();

        //String timestamp = Instant.now().getEpochSecond() + "";
        String resource = uri.toString().replace(getBaseUrl(), "");

        headers.add("apisign", EncryptionUtility.calculateHash(secret, uri, "HmacSHA512")); // Attaches signature as a header

        //request.addHeader("apisign", EncryptionUtility.calculateHash(secret, url, encryptionAlgorithm)); // Attaches signature as a header
        headers.add("accept", "application/json");
        headers.add("content-type", "application/json");

        curlRequest(method, jsonBody, headers, resource);

        return new HttpEntity<>(jsonBody, headers);
    }
}
