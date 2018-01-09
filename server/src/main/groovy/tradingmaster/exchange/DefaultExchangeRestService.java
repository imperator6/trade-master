package tradingmaster.exchange;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;


/**
 * Created by irufus on 2/25/15.
 */
@Component
public abstract class DefaultExchangeRestService implements IExchangeRestService {

    static Logger log = Logger.getLogger(DefaultExchangeRestService.class.getName());

    protected String publicKey;
    protected String secret;
    protected String passphrase;
    protected String baseUrl;

    protected RestTemplate restTemplate;

    public DefaultExchangeRestService( String publicKey,
                                       String secret,
                                       String passphrase,
                                       String baseUrl,
                                       RestTemplate restTemplate) {
        this.publicKey = publicKey;
        this.secret = secret;
        this.passphrase = passphrase;
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public <T> T get(String resourcePath, ParameterizedTypeReference<T> responseType) {
        try {
            ResponseEntity<T> responseEntity = restTemplate.exchange(getBaseUrl() + resourcePath,
                    GET,
                    securityHeaders(resourcePath,
                    "GET",
                     ""),
                    responseType);
            return responseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            log.error("GET request Failed for '" + resourcePath + "': " + ex.getResponseBodyAsString());
        }
        return null;
    }

    @Override
    public <T> List<T> getAsList(String resourcePath, ParameterizedTypeReference<T[]> responseType) {
       T[] result = get(resourcePath, responseType);

       return result == null ? Arrays.asList() : Arrays.asList(result);
    }

    @Override
    public <T> T pagedGet(String resourcePath,
                          ParameterizedTypeReference<T> responseType,
                          String beforeOrAfter,
                          Integer pageNumber,
                          Integer limit) {
        resourcePath += "?" + beforeOrAfter + "=" + pageNumber + "&limit=" + limit;
        return get(resourcePath, responseType);
    }

    @Override
    public <T> List<T> pagedGetAsList(String resourcePath,
                          ParameterizedTypeReference<T[]> responseType,
                          String beforeOrAfter,
                          Integer pageNumber,
                          Integer limit) {
        T[] result = pagedGet(resourcePath, responseType, beforeOrAfter, pageNumber, limit );
        return result == null ? Arrays.asList() : Arrays.asList(result);
    }

    @Override
    public <T> T delete(String resourcePath, ParameterizedTypeReference<T> responseType) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(getBaseUrl() + resourcePath,
                HttpMethod.DELETE,
                securityHeaders(resourcePath, "DELETE", ""),
                responseType);
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            log.error("DELETE request Failed for '" + resourcePath + "': " + ex.getResponseBodyAsString());
        }
        return null;
    }

    @Override
    public <T, R> T post(String resourcePath,  ParameterizedTypeReference<T> responseType, R jsonObj) {
        Gson gson = new Gson();
        String jsonBody = gson.toJson(jsonObj);

        try {
            ResponseEntity<T> response = restTemplate.exchange(getBaseUrl() + resourcePath,
                    HttpMethod.POST,
                    securityHeaders(resourcePath, "POST", jsonBody),
                    responseType);
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            log.error("POST request Failed for '" + resourcePath + "': " + ex.getResponseBodyAsString());
        }
        return null;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public abstract HttpEntity<String> securityHeaders(String endpoint, String method, String jsonBody);

    protected void curlRequest(String method, String jsonBody, HttpHeaders headers, String resource) {
        String curlTest = "curl ";
        for (String key : headers.keySet()){
            curlTest +=  "-H '" + key + ":" + headers.get(key).get(0) + "' ";
        }
        if (!jsonBody.equals(""))
            curlTest += "-d '" + jsonBody + "' ";

        curlTest += "-X " + method + " " + getBaseUrl() + resource;
        log.debug(curlTest);
    }
}
