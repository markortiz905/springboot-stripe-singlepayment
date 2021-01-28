package com.stripe.poc.prismstripesplitpayments.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountCollection;
import com.stripe.model.AccountLink;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
public class StripeOnboardingController {

    @Value("${stripe.api.key}")
    private String stripeKey;

    private Map<String, Account> map = null;

    @RequestMapping("stripe/createaccount")
    public String stripConnect(Model model) {
        model.addAttribute("tenantId", UUID.randomUUID().toString());
        model.addAttribute("uuid", UUID.randomUUID().toString());
        return "createaccount";
    }

    @Retryable( value = StripeException.class, maxAttemptsExpression = "${retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay}"))
    @RequestMapping("stripe/onboard/{tenantId}/{uuid}")
    public String createStripe(@PathVariable String tenantId, @PathVariable String uuid) throws StripeException {
        if (map == null) {
            map = new HashMap<>();
            Map<String, Object> params = new HashMap<>();
            params.put("limit", 3);

            AccountCollection accounts = Account.list(params);
            accounts.getData().stream().map(acc -> map.put(acc.getId(), acc));
        }

        RequestOptions options = RequestOptions
                .builder()
                .setIdempotencyKey(uuid)
                .setApiKey(stripeKey)
                .build();

        AccountCreateParams paramsCreateAccount =
                AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.CUSTOM)
                        .setCapabilities(AccountCreateParams.Capabilities.builder()
                                .setTransfers(AccountCreateParams.Capabilities.Transfers
                                        .builder().setRequested(true).build())
                                .setCardPayments(AccountCreateParams.Capabilities.CardPayments
                                        .builder().setRequested(true).build())
                                .build())
                        .setEmail("ortizmarkgaming1@gmail.com")
                        .build();

        Account account = Account.create(paramsCreateAccount, options);
        map.put(account.getId(), account);
        log.info("account.id: " + account.getId());

        AccountLinkCreateParams params =
                AccountLinkCreateParams.builder()
                        .setAccount(account.getId())
                        .setRefreshUrl("http://localhost:8080/stripe/reauth?id=" + account.getId())
                        .setReturnUrl("http://localhost:8080/stripe/return?id=" + account.getId())
                        .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                        .build();

        options = RequestOptions
                .builder()
                .setIdempotencyKey(uuid + "_second")
                .setApiKey(stripeKey)
                .build();

        log.info("stripeKey: " + stripeKey);
        AccountLink accountLink = AccountLink.create(params, options);

        log.info("redirectUrl: " + accountLink.getUrl());
        return "redirect:" + accountLink.getUrl();
    }
}
