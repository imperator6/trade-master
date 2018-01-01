package tradingmaster.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class RestTemplateConfig {

    @Value("${http.proxyHost}")
    String host;

    @Value("${http.proxyPort}")
    Integer port;

    @Bean
    public RestTemplate getRestTemplate() {

        RestTemplate restTemplate = new RestTemplate();

        if(host != null && port != null) {

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            InetSocketAddress address = new InetSocketAddress(host,port);
            Proxy proxy = new Proxy(Proxy.Type.HTTP,address);
            factory.setProxy(proxy);

            restTemplate.setRequestFactory(factory);
        }

        return restTemplate;
    }


}
