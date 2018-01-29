package tradingmaster.exchange.mininghamster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tradingmaster.exchange.DefaultExchangeRestService;
import tradingmaster.exchange.bittrex.EncryptionUtility;
import tradingmaster.exchange.mininghamster.model.HamsterSignal;

import java.util.List;
import java.util.Map;

@Service
class MiningHamster extends DefaultExchangeRestService {

    static Log LOG = LogFactory.getLog(MiningHamster.class);

    @Autowired
    public MiningHamster(@Value("${mininghamster.key}") String publicKey,
                               @Value("${mininghamster.api.baseUrl}") String baseUrl,
                               RestTemplate restTemplate) {

        super(publicKey, null, null, baseUrl, restTemplate);
    }

    @Override
    public Map addAdditionalParmas(String resourcePath, Map params) {

        if(!(resourcePath.indexOf("public") > -1)) {
            params.put("apikey", publicKey);
        }
        return params;
    }



    @Override
    public HttpEntity<String> securityHeaders(String uri, String resourcePath, String method, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();

        //String timestamp = Instant.now().getEpochSecond() + "";
        String resource = uri.toString().replace(getBaseUrl(), "");

        headers.add("apisign", EncryptionUtility.calculateHash(publicKey, uri, "HmacSHA512")); // Attaches signature as a header

        //request.addHeader("apisign", EncryptionUtility.calculateHash(secret, url, encryptionAlgorithm)); // Attaches signature as a header
        headers.add("accept", "application/json");
        headers.add("content-type", "application/json");

        curlRequest(method, jsonBody, headers, resource);

        return new HttpEntity<>(jsonBody, headers);
    }

    public List<HamsterSignal> getLatestSignals() {


        List<HamsterSignal> signals = get("", new ParameterizedTypeReference<List<HamsterSignal>>(){});

        if(signals != null)
            LOG.info("Fetched Hamster Signals! -> " + signals.size());

        return signals;
    }

}
