package com.preciazo.platform.shopping.infrastructure.time;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ShoppingClockConfiguration {
    @Bean public Clock shoppingClock(@Value("${shopping.default-zone:America/Lima}") String zone) { return Clock.system(ZoneId.of(zone)); }
}
