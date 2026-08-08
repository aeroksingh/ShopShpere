package com.shopsphere.shopsphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @SpringBootApplication is a convenience annotation that bundles THREE things:
 *   1. @Configuration      -> this class can define @Bean methods
 *   2. @EnableAutoConfiguration -> Spring Boot guesses beans you need based on
 *                                  what's on the classpath (e.g. sees postgresql
 *                                  driver -> configures a DataSource automatically)
 *   3. @ComponentScan      -> scans this package + sub-packages for @Component,
 *                             @Service, @Repository, @RestController etc.
 *
 * This is why package placement matters: anything OUTSIDE com.shopsphere.shopsphere
 * will NOT be picked up unless you scan it explicitly.
 */
@SpringBootApplication
@EnableCaching
public class ShopSphereApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopSphereApplication.class, args);
    }

}
