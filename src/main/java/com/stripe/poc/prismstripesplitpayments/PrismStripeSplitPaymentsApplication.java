package com.stripe.poc.prismstripesplitpayments;

import com.stripe.exception.StripeException;
import com.stripe.poc.prismstripesplitpayments.service.StripeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;

import java.util.UUID;

@Slf4j
@EnableRetry
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class PrismStripeSplitPaymentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrismStripeSplitPaymentsApplication.class, args);
	}

	@Bean
	@Profile("testCharge")
	public CommandLineRunner testCharge(StripeService stripeService) {
		return (args) -> {

			try {
				stripeService.chargeSource("tok_visa", 2000L, "Test charging 2000 usd",
						"usd", "ortizmark905@gmail.com", UUID.randomUUID().toString());
			} catch (StripeException e) {
				log.error(e.getMessage(), e);
			}
		};
	}

}
