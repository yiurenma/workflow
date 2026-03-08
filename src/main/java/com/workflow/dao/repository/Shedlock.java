package com.workflow.dao.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "SHEDLOCK")
public class Shedlock {

    @Id
    @Column(name = "NAME", nullable = false, length = 64)
    private String name;

    @Column(name = "LOCKED_AT", nullable = false)
    private Date lockedAt;

    @Column(name = "LOCK_UNTIL", nullable = false)
    private Date lockUntil;

    @Column(name = "LOCKED_BY", nullable = false, length = 255)
    private String lockedBy;

}
