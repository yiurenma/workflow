package com.workflow.dao.repository;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name = "WORKFLOW_RECORD")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@DynamicUpdate
public class WorkflowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String applicationName;
    private String requestCorrelationId;
    private String transactionConfirmationNumber;
    private String workflowLinkingId;
    private String trackingNumber;
    @Lob
    private String workflowTransactionDetails;
    @Lob
    private String workflowResponseFromProvider;
    private String workflowProvider;
    private String customerId;
    private String overallStatus;
    private String smsStatus;
    private String emailStatus;
    private String pushNotificationStatus;
    private String pushNotificationDetailStatus;
    @Lob
    private String providerDescription;
    @Column(nullable = true)
    private Integer retryTimes;
    @Column(nullable = true)
    private Long originWorkflowRecordId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE_TIME", nullable = false, updatable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ssXXX")
    private Date createdDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_DATE_TIME", nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ssXXX")
    private Date lastModifiedDateTime;

    @PrePersist
    protected void onCreate() {
        lastModifiedDateTime = createdDateTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDateTime = new Date();
    }
}
