package az.abb.embassyflow.customer.web.dto.response;

import java.util.List;

public record CustomerAccountsResponse(List<AccountResponse> accounts) {
}