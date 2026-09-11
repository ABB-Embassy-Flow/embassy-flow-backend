package az.abb.embassyflow.customer.web.dto.response;

import az.abb.embassyflow.customer.domain.AccountType;
import az.abb.embassyflow.customer.domain.Currency;

import java.math.BigDecimal;
import java.util.List;

public record AccountResponse(Long id,
                              String accountNumber,
                              Currency currency,
                              BigDecimal balance,
                              AccountType type,
                              List<CardResponse> cards) {
}