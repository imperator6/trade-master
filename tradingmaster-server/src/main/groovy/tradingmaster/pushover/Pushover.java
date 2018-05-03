package tradingmaster.pushover;

import com.rwe.platform.rest.DefaultRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class Pushover extends DefaultRestService {

    @Value("${pushover.device}")
    String device;

    @Value("${pushover.enabled}")
    Boolean enabled;

    @Autowired
    public Pushover(@Value("${pushover.token}") String publicKey,
                               @Value("${pushover.user}") String secret,
                               @Value("${pushover.api.baseUrl}") String baseUrl,
                               RestTemplate restTemplate) {

        super(publicKey, secret, null, baseUrl, restTemplate);
    }

    public void sendMessage(String title, String msg, String url, String urlTitle) {

        if(!enabled) {
            return;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("token", publicKey);
        params.put("user", secret);
        params.put("device", device);
        params.put("message", msg);
        params.put("title", title);
        params.put("html", "1");
        params.put("sound", "cashregister");
        if(urlTitle != null) {
            params.put("url_title", urlTitle);
        }

        if(url != null) {
            params.put("url", url);
        }

        try {

            super.post("", new HashMap(), new ParameterizedTypeReference<String>() {}, params);
        } catch(Exception e) {
            log.error("Pushover Error", e);
        }
    }




    @Override
    public HttpEntity<String> securityHeaders(String uri, String resourcePath, String method, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();

        //request.addHeader("apisign", EncryptionUtility.calculateHash(secret, url, encryptionAlgorithm)); // Attaches signature as a header
        headers.add("accept", "application/json");
        headers.add("content-type", "application/json");

        return new HttpEntity<>(jsonBody, headers);
    }
}
