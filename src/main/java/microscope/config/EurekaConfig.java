package microscope.config;

import com.netflix.appinfo.AmazonInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.bind.RelaxedNames;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.cloud.netflix.eureka.metadata.ManagementMetadata;
import org.springframework.cloud.netflix.eureka.metadata.ManagementMetadataProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.annotation.PostConstruct;

import static com.netflix.appinfo.AmazonInfo.MetaDataKey.localHostname;
import static com.netflix.appinfo.AmazonInfo.MetaDataKey.localIpv4;

/**
 * Hacking the {@link EurekaInstanceConfigBean} to contain AWS info and fixing ports and urls
 *
 * @author Jonatan Ivanov
 */
@Configuration
@AutoConfigureBefore({SimpleDiscoveryClientAutoConfiguration.class, CommonsClientAutoConfiguration.class, ServiceRegistryAutoConfiguration.class })
public class EurekaConfig {
    private static final String EUREKA_NAMESPACE = "eureka";
    private static final String SSL_ENABLED_KEY = "server.ssl.enabled";
    private static final String PREFER_IP_KEY = "eureka.instance.preferIpAddress";
    private static final String HOSTNAME_KEY = "eureka.instance.hostname";
    private static final String IP_KEY = "eureka.instance.ipAddress";
    private static final String MGMT_CONTEXT_PATH_KEY = "management.contextPath";
    /** spring-boot admin needs this key: "management.context-path", it does not support {@link RelaxedNames} :( */
    private static final String SBA_MGMT_CONTEXT_PATH_KEY = "management.context-path";
    private static final String SECURE_PORT_ENABLED_KEY = "eureka.instance.securePortEnabled";
    private static final String NON_SECURE_PORT_ENABLED_KEY = "eureka.instance.nonSecurePortEnabled";
    private static final String SECURE_PORT_KEY = "eureka.instance.securePort";
    private static final String SERVER_PORT_KEY = "server.port";
    private static final String PORT_KEY = "port";
    private static final int DEFAULT_PORT = 8080;
    private static final String SERVER_CONTEXT_PATH_KEY = "server.contextPath";
    private static final String DEFAULT_CONTEXT_PATH = "/";
    private static final String MGMT_PORT_KEY = "management.port";
    private static final String STATUS_PAGE_URL_KEY = "eureka.instance.statusPageUrl";
    private static final String HEALTH_CHECK_URL_KEY = "eureka.instance.healthCheckUrl";
    private static final String SECURE_HEALTH_CHECK_URL_KEY = "eureka.instance.secureHealthCheckUrl";
    private static final String HOME_PAGE_URL_KEY = "eureka.instance.homePageUrl";

    @Autowired private EurekaInstanceConfigBean instance;
    @Autowired private ManagementMetadataProvider metadataProvider;
    @Autowired(required = false) private AmazonInfo amazonInfo;

    private final RelaxedPropertyResolver propertyResolver;
    private final boolean sslEnabled;
    private final int serverPort;

    public EurekaConfig(ConfigurableEnvironment env) {
        this.propertyResolver = new RelaxedPropertyResolver(env);
        this.sslEnabled = getProperty(SSL_ENABLED_KEY, Boolean.class, false);
        this.serverPort = getProperty(SERVER_PORT_KEY, Integer.class, getProperty(PORT_KEY, Integer.class, DEFAULT_PORT));
    }

    @Bean
    @ConditionalOnMissingBean(AmazonInfo.class)
    public AmazonInfo amazonInfo() {
        return AmazonInfo.Builder.newBuilder().autoBuild(EUREKA_NAMESPACE);
    }

    @PostConstruct
    public void fixEurekaProperties() {
        addAmazonInfo();
        populateMetadataMap();
        fixPorts();
        fixUrls();
        instance.setPreferIpAddress(getProperty(PREFER_IP_KEY, Boolean.class, true));
    }

    private void addAmazonInfo() {
        if (!amazonInfo.getMetadata().isEmpty()) {
            instance.setDataCenterInfo(amazonInfo);
            instance.setHostname(getProperty(HOSTNAME_KEY, amazonInfo.get(localHostname)));
            instance.setIpAddress(getProperty(IP_KEY, amazonInfo.get(localIpv4)));
        }
    }

    private void populateMetadataMap() {
        instance.getMetadataMap().put(SBA_MGMT_CONTEXT_PATH_KEY, getProperty(MGMT_CONTEXT_PATH_KEY));
    }

    private void fixPorts() {
        if (sslEnabled) {
            instance.setSecurePortEnabled(getProperty(SECURE_PORT_ENABLED_KEY, Boolean.class, true));
            instance.setNonSecurePortEnabled(getProperty(NON_SECURE_PORT_ENABLED_KEY, Boolean.class, false));
            instance.setSecurePort(getProperty(SECURE_PORT_KEY, Integer.class, serverPort));
        }
    }

    private void fixUrls() {
        String serverContextPath = getProperty(SERVER_CONTEXT_PATH_KEY, DEFAULT_CONTEXT_PATH);
        String managementContextPath = getProperty(MGMT_CONTEXT_PATH_KEY);
        Integer managementPort = getProperty(MGMT_PORT_KEY, Integer.class, serverPort);

        ManagementMetadata metadata = metadataProvider.get(instance, serverPort, serverContextPath, managementContextPath, managementPort);
        if (metadata != null) {
            instance.setStatusPageUrl(getProperty(STATUS_PAGE_URL_KEY, metadata.getStatusPageUrl()));
            instance.setHealthCheckUrl(getProperty(HEALTH_CHECK_URL_KEY, metadata.getHealthCheckUrl()));
            instance.setHomePageUrl(getProperty(HOME_PAGE_URL_KEY, metadata.getStatusPageUrl()));
            if (sslEnabled) {
                instance.setSecureHealthCheckUrl(getProperty(SECURE_HEALTH_CHECK_URL_KEY, metadata.getHealthCheckUrl()));
            }
        }
    }

    private String getProperty(String key) {
        return getProperty(key, String.class,null);
    }

    private String getProperty(String key, String defaultValue) {
        return getProperty(key, String.class, defaultValue);
    }

    private <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return propertyResolver.getProperty(key, targetType, defaultValue);
    }
}
