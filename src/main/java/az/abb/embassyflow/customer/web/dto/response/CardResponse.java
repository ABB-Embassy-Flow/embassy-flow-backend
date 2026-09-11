package az.abb.embassyflow.customer.web.dto.response;

public record CardResponse(Long id, String maskedNumber, String brand, String expiry) {
}