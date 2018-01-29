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

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return get(resourcePath, new HashMap<String, String>(), responseType);
    }

    @Override
    public <T> T get(String resourcePath, Map<String, ?> params, ParameterizedTypeReference<T> responseType) {
        try {

            addAdditionalParmas(resourcePath, params);

           /* UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromPath(resourcePath);

            for(String key: params.keySet()){
                Object value= params.get(key);
                urlBuilder.queryParam(key, value);
            } */

            String url = buildQueryString(resourcePath, params);

            URI uri = new URI(url);

           /* ResponseEntity<String> response
                    =  restTemplate.exchange(uri ,
                    GET,
                    securityHeaders(uri.toString(), resourcePath,
                            "GET",
                            ""),
                    new ParameterizedTypeReference<String>(){}); */

            //String uri = buildUrl(urlBuilder.toUriString());
            ResponseEntity<T> responseEntity = restTemplate.exchange(uri ,
                    GET,
                    securityHeaders(uri.toString(), resourcePath,
                    "GET",
                     ""),
                    responseType);
            return responseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            log.error("GET request Failed for '" + resourcePath + "': " + ex.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("GET request Failed for '" + resourcePath + "': " + e.getMessage());
        }
        return null;
    }

    @Override
    public <T> List<T> getAsList(String resourcePath, ParameterizedTypeReference<T[]> responseType) {
        T[] result = get(resourcePath, new HashMap<String, String>(), responseType);

        return result == null ? Arrays.asList() : Arrays.asList(result);
    }

    @Override
    public <T> List<T> getAsList(String resourcePath, Map<String, ?> uriVariables, ParameterizedTypeReference<T[]> responseType) {
       T[] result = get(resourcePath, uriVariables, responseType);

       return result == null ? Arrays.asList() : Arrays.asList(result);
    }

    @Override
    public <T> T pagedGet(String resourcePath,
                          Map<String, ?> uriVariables,
                          ParameterizedTypeReference<T> responseType,
                          String beforeOrAfter,
                          Integer pageNumber,
                          Integer limit) {
        resourcePath += "?" + beforeOrAfter + "=" + pageNumber + "&limit=" + limit;
        return get(resourcePath, uriVariables, responseType);
    }

    @Override
    public <T> List<T> pagedGetAsList(String resourcePath,
                                      Map<String, ?> uriVariables,
                          ParameterizedTypeReference<T[]> responseType,
                          String beforeOrAfter,
                          Integer pageNumber,
                          Integer limit) {
        T[] result = pagedGet(resourcePath, uriVariables, responseType, beforeOrAfter, pageNumber, limit );
        return result == null ? Arrays.asList() : Arrays.asList(result);
    }

    @Override
    public <T> T delete(String resourcePath, ParameterizedTypeReference<T> responseType) {
        return delete(resourcePath, new HashMap<String, String>(), responseType);
    }

    @Override
    public <T> T delete(String resourcePath, Map<String, ?> params, ParameterizedTypeReference<T> responseType) {
        try {
            addAdditionalParmas(resourcePath, params);

            String url = buildQueryString(resourcePath, params);

            URI uri = new URI(url);
            ResponseEntity<T> response = restTemplate.exchange(uri,
                HttpMethod.DELETE,
                securityHeaders(uri.toString(), resourcePath, "DELETE", ""),
                responseType);
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            log.error("DELETE request Failed for '" + resourcePath + "': " + ex.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("DELETE request Failed for '" + resourcePath + "': " + e.getMessage());
        }

        return null;
    }

    @Override
    public <T, R> T post(String resourcePath, ParameterizedTypeReference<T> responseType, R jsonObj) {
       return post(resourcePath, new HashMap<String, String>(), responseType, jsonObj);
    }

    @Override
    public <T, R> T post(String resourcePath,  Map<String, ?> params, ParameterizedTypeReference<T> responseType, R jsonObj) {
        Gson gson = new Gson();
        String jsonBody = gson.toJson(jsonObj);
        try {

            addAdditionalParmas(resourcePath, params);

            String url = buildQueryString(resourcePath, params);

            URI uri = new URI(url);

            ResponseEntity<T> response = restTemplate.exchange(uri,
                    HttpMethod.POST,
                    securityHeaders(uri.toString(), resourcePath, "POST", jsonBody),
                    responseType);

            return response.getBody();
        } catch (HttpClientErrorException ex) {
            log.error("POST request Failed for '" + resourcePath + "': " + ex.getResponseBodyAsString());
        }  catch (Exception e) {
            log.error("POST request Failed for '" + resourcePath + "': "  + e.getMessage());
        }
        return null;
    }


    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    public Map addAdditionalParmas(String resourcePath, Map params) {
        return params;
    }

    @Override
    public abstract HttpEntity<String> securityHeaders(String uri, String resourcePath, String method, String jsonBody);

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

    private String buildQueryString(String resourcePath, Map<String,?> params) {

        boolean first = true;
        String result = getBaseUrl() + resourcePath;

        for(String key: params.keySet()){
            Object value= params.get(key);

            if(first) {
                result += "?" + key + "=" + value;
                first = false;
            } else {
                result += "&" + key + "=" + value;
            }
        }

        return result;

    }
}
