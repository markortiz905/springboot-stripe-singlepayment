package com.stripe.poc.prismstripesplitpayments.service;

import com.stripe.Stripe;
import com.stripe.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import com.stripe.param.ChargeCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StripeService {
    @Value("${stripe.api.key}")
    private String stripeKey;

    @Retryable( value = StripeException.class, maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay}"))
    public Charge chargeSource(String source, Long amount, String description, String currency, String receiptEmail, String uuid) throws StripeException {
        // `source` is obtained with Stripe.js; see https://stripe.com/docs/payments/accept-a-payment-charges#web-create-token
        log.info("Creating charge for source: " + source + " with idempotencykey: " + uuid);

        Stripe.apiKey = stripeKey;

        ChargeCreateParams cParam = ChargeCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setDescription(description)
                .setReceiptEmail(receiptEmail)
                .setSource(source).build();

        RequestOptions options = RequestOptions
                .builder()
                .setIdempotencyKey(uuid)
                .setApiKey(stripeKey)
                .build();

        Charge charge = Charge.create(cParam, options);

        log.info(Charge.retrieve(charge.getId()).getPaid().toString());
        throw new ApiException("test", "test", "test", 1, new Exception());
        //return charge;
    }
}
