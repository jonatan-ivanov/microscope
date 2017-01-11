package microscope.config;

import de.codecentric.boot.admin.notify.Notifier;
import de.codecentric.boot.admin.notify.RemindingNotifier;
import de.codecentric.boot.admin.notify.filter.FilteringNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Jonatan Ivanov
 */
@Configuration
@EnableScheduling
public class NotifierConfiguration {
    @Autowired private Notifier notifier;

    @Bean
    public FilteringNotifier filteringNotifier() {
        //TODO: figure out something better e.g.: conditional bean creation
        Notifier delegate = (notifier != null) ? notifier : event -> {};
        return new FilteringNotifier(delegate);
    }

    @Primary @Bean
    public RemindingNotifier remindingNotifier() {
        return new RemindingNotifier(filteringNotifier());
    }

    @Scheduled(fixedRateString = "${spring.boot.admin.notify.scheduler.fixedRate}")
    public void remind() {
        remindingNotifier().sendReminders();
    }
}
