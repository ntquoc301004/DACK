package DACK.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SampleDataLoader implements CommandLineRunner {

    private final SampleDataSeedService sampleDataSeedService;

    @Override
    public void run(String... args) {
        sampleDataSeedService.seedIfEnabled();
    }
}
