package microscope.config;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Jonatan Ivanov
 */
@Configuration
@ConditionalOnProperty("spring.boot.admin.discovery.ignore-ssl-errors")
public class IgnoreSslErrorsConfig {
    @Autowired private ZuulProperties zuulProperties;

    @Bean
    public RestTemplateBuilder restTemplateBuilder() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();
        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, (hostname, session) -> true))
            .build();

        return new RestTemplateBuilder().requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    @PostConstruct
    public void disableZuulSslHostnameValidation() {
        zuulProperties.setSslHostnameValidationEnabled(false);
    }
}
