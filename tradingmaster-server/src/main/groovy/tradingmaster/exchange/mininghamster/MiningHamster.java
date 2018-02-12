package tradingmaster.exchange.mininghamster;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.rwe.platform.rest.DefaultRestService;
import tradingmaster.exchange.bittrex.EncryptionUtility;
import tradingmaster.exchange.mininghamster.model.HamsterSignal;

import java.util.List;
import java.util.Map;

@Service
class MiningHamster extends DefaultRestService {

    static Log LOG = LogFactory.getLog(MiningHamster.class);

    ObjectMapper mapper;

    @Autowired
    public MiningHamster(@Value("${mininghamster.key}") String publicKey,
                               @Value("${mininghamster.api.baseUrl}") String baseUrl,
                               RestTemplate restTemplate) {

        super(publicKey, null, null, baseUrl, restTemplate);

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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


        //List<HamsterSignal> signals = get("", new ParameterizedTypeReference<List<HamsterSignal>>(){});

       String signalSt = get("", new ParameterizedTypeReference<String>(){});

        List<HamsterSignal> signals = null;
        try {
            signals = mapper.readValue(signalSt ,  new TypeReference<List<HamsterSignal>>(){});
        } catch(Exception e) {
            LOG.error("Can't parse hamster signal rest response! " + signalSt);
        }

        if(signals != null)
            LOG.info("Fetched Hamster Signals! -> " + signals.size());

        return signals;
    }

}
