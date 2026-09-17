package az.abb.embassyflow.order.service;

import az.abb.embassyflow.common.exception.BusinessException;
import az.abb.embassyflow.common.exception.ErrorCodes;
import az.abb.embassyflow.customer.dto.response.AccountResponse;
import az.abb.embassyflow.customer.service.CustomerService;
import az.abb.embassyflow.embassy.service.EmbassyService;
import az.abb.embassyflow.order.dao.entity.DocumentOrder;
import az.abb.embassyflow.order.dao.entity.OrderItem;
import az.abb.embassyflow.order.dao.repository.DocumentOrderRepository;
import az.abb.embassyflow.order.dto.response.PreviewResponse;
import az.abb.embassyflow.order.enums.Language;
import az.abb.embassyflow.order.enums.OrderStatus;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final int QR_SIZE = 240;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneOffset.UTC);

    private final DocumentOrderRepository orderRepository;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final EmbassyService embassyService;
    private final String verifyBaseUrl;

    public DocumentService(DocumentOrderRepository orderRepository, OrderService orderService,
                           CustomerService customerService, EmbassyService embassyService,
                           @Value("${app.verify-base-url:http://localhost:8080/api/v1/portal/documents}")
                           String verifyBaseUrl) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.customerService = customerService;
        this.embassyService = embassyService;
        this.verifyBaseUrl = verifyBaseUrl;
    }

    @Transactional
    public PreviewResponse generatePreview(Long orderId, Long authenticatedCustomerId) {
        DocumentOrder order = orderService.requireOwnedOrder(orderId, authenticatedCustomerId);

        if (order.getStatus() != OrderStatus.OTP_VERIFIED || order.getItems().isEmpty()) {
            throw new BusinessException(ErrorCodes.CONFLICT, "error.conflict", HttpStatus.CONFLICT);
        }

        String verificationCode = ensureVerificationCode(order);
        String html = buildHtml(order, verificationCode);
        String qrCode = toQrDataUri(verifyBaseUrl + "/" + order.getOrderNumber() + "/verify?code=" + verificationCode);

        return new PreviewResponse(order.getOrderNumber(), html, qrCode, verificationCode);
    }

    private String ensureVerificationCode(DocumentOrder order) {
        if (order.getVerificationCode() != null) {
            return order.getVerificationCode();
        }

        String code;
        do {
            code = randomCode();
        } while (orderRepository.existsByVerificationCode(code));

        order.setVerificationCode(code);
        return code;
    }

    private static String randomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }

    private String buildHtml(DocumentOrder order, String verificationCode) {
        boolean az = order.getLanguage() == Language.AZ;
        String title = az ? "Səfirliyə arayış" : "Reference Letter";
        String customerLabel = az ? "Müştəri" : "Customer";
        String embassyLabel = az ? "Səfirlik" : "Embassy";
        String dateLabel = az ? "Tarix" : "Date";
        String totalLabel = az ? "Ümumi məbləğ" : "Total";
        String codeLabel = az ? "Yoxlama kodu" : "Verification code";
        String noLabel = az ? "Sənəd No" : "Document No";

        String customerName = customerService.findById(order.getCustomerId())
                .map(CustomerService.CustomerInfo::fullName)
                .orElse("");
        String embassyName = order.getEmbassyId() == null
                ? ""
                : embassyService.findName(order.getEmbassyId()).orElse("");

        Map<Long, AccountResponse> accountsById = customerService
                .accountsFor(order.getCustomerId(), order.getCustomerId()).stream()
                .collect(Collectors.toMap(AccountResponse::id, Function.identity()));

        StringBuilder rows = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            AccountResponse account = accountsById.get(item.getAccountId());
            String accountNumber = account == null ? String.valueOf(item.getAccountId()) : account.accountNumber();
            String currency = account == null ? "" : account.currency().name();
            rows.append("<tr>")
                    .append("<td>").append(escape(accountNumber)).append("</td>")
                    .append("<td>").append(escape(currency)).append("</td>")
                    .append("<td>").append(escape(item.getPeriod().getCode())).append("</td>")
                    .append("<td>").append(escape(item.getStatementType().name())).append("</td>")
                    .append("<td>").append(item.isEquivalentCurrency() ? (az ? "Bəli" : "Yes") : (az ? "Xeyr" : "No"))
                    .append("</td>")
                    .append("</tr>");
        }

        String head = "<th>" + (az ? "Hesab" : "Account") + "</th><th>" + (az ? "Valyuta" : "Currency")
                + "</th><th>" + (az ? "Müddət" : "Period") + "</th><th>" + (az ? "Növ" : "Type")
                + "</th><th>" + (az ? "Ekvivalent" : "Equivalent") + "</th>";

        return "<div style=\"font-family:Arial,Helvetica,sans-serif;max-width:640px;margin:0 auto;"
                + "padding:24px;border:1px solid #e0e0e0;\">"
                + "<h2 style=\"color:#e30613;margin:0 0 4px;\">ABB</h2>"
                + "<h3 style=\"margin:0 0 16px;\">" + title + "</h3>"
                + "<p><b>" + noLabel + ":</b> " + escape(order.getOrderNumber()) + "</p>"
                + "<p><b>" + dateLabel + ":</b> "
                + (order.getCreatedAt() == null ? "" : DATE_FORMAT.format(order.getCreatedAt())) + "</p>"
                + "<p><b>" + customerLabel + ":</b> " + escape(customerName) + "</p>"
                + (embassyName.isEmpty() ? "" : "<p><b>" + embassyLabel + ":</b> " + escape(embassyName) + "</p>")
                + "<table width=\"100%\" cellspacing=\"0\" cellpadding=\"6\" "
                + "style=\"border-collapse:collapse;\" border=\"1\"><thead><tr>" + head + "</tr></thead>"
                + "<tbody>" + rows + "</tbody></table>"
                + "<p><b>" + totalLabel + ":</b> " + order.getDocumentType().getPrice() + " "
                + order.getDocumentType().getCurrency() + "</p>"
                + "<p style=\"font-size:12px;color:#666;\">" + codeLabel + ": " + verificationCode + "</p>"
                + "</div>";
    }

    private static String toQrDataUri(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < matrix.getWidth(); x++) {
                for (int y = 0; y < matrix.getHeight(); y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (WriterException | IOException ex) {
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "error.generic",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
