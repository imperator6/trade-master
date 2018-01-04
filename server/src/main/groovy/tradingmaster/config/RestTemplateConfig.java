package tradingmaster.config;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.security.cert.X509Certificate;

@Configuration
public class RestTemplateConfig {

    @Value("${http.proxyHost}")
    String host;

    @Value("${http.proxyPort}")
    Integer port;


    TrustStrategy trustStrategy = new TrustStrategy() {
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType) {
            return true;
        }
    };

    HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    @Bean
    public RestTemplate getRestTemplate() throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        // ignore ssl certificate in order to work with binance
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                new SSLContextBuilder().loadTrustMaterial(trustStrategy).build(), hostnameVerifier);
        HttpClientBuilder clientBuilder = HttpClients.custom().setSSLSocketFactory(socketFactory);

        if(host != null && port != null) {
            clientBuilder = clientBuilder.setProxy(new HttpHost(host, port));
        }

        HttpClient httpClient = clientBuilder.build();
        factory.setHttpClient(httpClient);

        restTemplate.setRequestFactory(factory);


        return restTemplate;
    }


}
