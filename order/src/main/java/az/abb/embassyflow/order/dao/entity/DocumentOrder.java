package az.abb.embassyflow.order.dao.entity;

import az.abb.embassyflow.common.dao.entity.AuditableEntity;
import az.abb.embassyflow.order.enums.DocumentType;
import az.abb.embassyflow.order.enums.Language;
import az.abb.embassyflow.order.enums.OrderStatus;
import az.abb.embassyflow.order.enums.TimelineStep;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "document_orders")
public class DocumentOrder extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 10)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OrderStatus status;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderTimeline> timeline = new ArrayList<>();

    public void addTimeline(TimelineStep step) {
        OrderTimeline entry = new OrderTimeline();
        entry.setOrder(this);
        entry.setStep(step);
        this.timeline.add(entry);
    }

    public Long getId() {
        return id;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public List<OrderTimeline> getTimeline() {
        return timeline;
    }
}