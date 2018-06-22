package microscope.manage;

import com.netflix.appinfo.AmazonInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author Jonatan Ivanov
 */
@Component
@ConditionalOnBean(AmazonInfo.class)
public class AwsInfoContributor implements InfoContributor {
    @Autowired private AmazonInfo amazonInfo;

    @Override
    public void contribute(Builder builder) {
        if (!amazonInfo.getMetadata().isEmpty()) {
            builder.withDetail("aws", amazonInfo.getMetadata());
        }
    }
}
