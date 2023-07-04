package ru.nspk.performance.theatre;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.nspk.performance.theatre.properties.CacheProperties;
import ru.nspk.performance.theatre.service.ReserveCache;

@Profile("cache")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CacheConfig.class})
@EnableConfigurationProperties({CacheProperties.class})
public class CacheTest {

    @Autowired
    ReserveCache reserveCache;
    @Autowired
    private CacheProperties cacheProperties;

    @Test
    void test() {
        reserveCache.hashCode();
    }
}
