package one.stayfocused.backend.config;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@Configuration
@PropertySource("classpath:.env")
public class DotenvLoader {

    @PostConstruct
    public void loadEnvVariables() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        dotenv.entries().forEach(entry -> {
            log.debug(entry.getKey(), entry.getValue());
            log.debug("Uploaded variable: {} = ******", entry.getKey());
        });
    }
}
