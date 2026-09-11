package az.abb.embassyflow.order.enums;

public enum TimelineStep {
    ORDER_RECEIVED,
    OTP_VERIFIED,
    PAYMENT_RECEIVED,
    ABB_APPROVED,
    DIGITALLY_SIGNED,
    DELIVERED_TO_EMBASSY,
    EMBASSY_REVIEWED
}