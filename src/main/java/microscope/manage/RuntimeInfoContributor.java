package microscope.manage;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * @author Jonatan Ivanov
 */
@Slf4j
@Component
public class RuntimeInfoContributor implements InfoContributor {
    private static final String DEFAULT_HOSTNAME = "unknown";
    private static final String DEFAULT_IP_ADDRESS = "unknown";

    @Autowired private Environment environment;
    private final Date startDate = new Date();

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("environment",
                ImmutableMap.of("activeProfiles", environment.getActiveProfiles())
        );
        builder.withDetail("network",
                ImmutableMap.builder()
                        .put("host", getSafeHostName())
                        .put("ip", getSafeIpAddress())
                        .build()
        );
        builder.withDetail("runtime",
                ImmutableMap.builder()
                        .put("java.version", System.getProperty("java.version"))
                        .put("startDate", startDate)
                        .put("heartBeat", new Date())
                        .put("uptime", getUptime())
                        .build()
        );
    }

    private String getUptime() {
        return Duration.between(startDate.toInstant(), Instant.now()).toString();
    }

    private String getSafeHostName() {
        String hostname = DEFAULT_HOSTNAME;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        }
        catch (Throwable e) {
            log.error("Unable to get the hostname", e);
        }

        return hostname;
    }

    private String getSafeIpAddress() {
        String ip = DEFAULT_IP_ADDRESS;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        }
        catch (Throwable e) {
            log.error("Unable to get the IP address", e);
        }

        return ip;
    }
}
