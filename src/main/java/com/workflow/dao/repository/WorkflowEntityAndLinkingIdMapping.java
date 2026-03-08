package com.workflow.dao.repository;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "WORKFLOW_ENTITY_AND_LINKING_ID_MAPPING")
@DynamicUpdate
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@EntityListeners(AuditingEntityListener.class)
public class WorkflowEntityAndLinkingIdMapping extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String workflowRuleAndTypeLinkingId;

    @ManyToOne
    @JoinColumn(name = "workflow_entity_setting_id")
    private WorkflowEntitySetting workflowEntitySetting;

    private Integer logicOrder;
    private String remark;
}
