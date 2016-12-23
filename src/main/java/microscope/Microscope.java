package microscope;

import de.codecentric.boot.admin.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Jonatan Ivanov
 */
@EnableAdminServer
@SpringBootApplication
public class Microscope {
    public static void main(String[] args) {
        SpringApplication.run(Microscope.class, args);
    }
}
