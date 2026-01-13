package com.in4everyall.tennisclubmanager.tennisclub.entity;

import com.in4everyall.tennisclubmanager.tennisclub.entity.base.AuditableEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentMethod;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "ix_payments_date", columnList = "payment_date"),
    @Index(name = "ix_payments_status", columnList = "status"),
    @Index(name = "ix_payments_contract", columnList = "contract_id"),
    @Index(name = "ix_payments_user", columnList = "user_email"),
    @Index(name = "ix_payments_number", columnList = "payment_number")
})
public class PaymentEntity extends AuditableEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = PaymentStatus.PENDING;
        if (paymentNumber == null) {
            // Generar paymentNumber usando fecha y parte del UUID
            String dateStr = LocalDate.now().toString().replace("-", "");
            String uuidStr = id.toString().replace("-", "").substring(0, 8).toUpperCase();
            paymentNumber = "PAY-" + dateStr + "-" + uuidStr;
        }
    }
    
    // Campos nuevos para el sistema de contratos
    @Column(name = "payment_number", length = 50, unique = true)
    private String paymentNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contract_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_payment_contract")
    )
    private ContractEntity contract;
    
    @Column(name = "user_email", length = 150)
    private String userEmail;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PaymentMethod paymentMethod;
    
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "EUR";
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
    
    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;
    
    @Column(name = "invoice_date")
    private LocalDate invoiceDate;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Column(name = "notes", length = 500)
    private String notes;
}


