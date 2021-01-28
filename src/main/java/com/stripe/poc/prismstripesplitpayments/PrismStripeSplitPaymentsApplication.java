package com.stripe.poc.prismstripesplitpayments;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.poc.prismstripesplitpayments.service.StripeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.UUID;

@Slf4j
@EnableRetry
//@EnableWebMvc
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class PrismStripeSplitPaymentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrismStripeSplitPaymentsApplication.class, args);
	}

	@Bean
	public CommandLineRunner initStripe(@Value("${stripe.api.key}") String key) {
		return (args) -> {
			log.info("stripe pub api key -> " + key);
			Stripe.apiKey = key;
		};
	}

	@Bean
	@Profile("testCharge")
	public CommandLineRunner testCharge(StripeService stripeService) {
		return (args) -> {

			try {
				stripeService.chargeSource("acct_1IE44AGOWnXIxCYu", 2000L, "Test charging 2000 usd",
						"usd", "ortizmark905@gmail.com", UUID.randomUUID().toString());
			} catch (StripeException e) {
				log.error(e.getMessage(), e);
			}
		};
	}

}
