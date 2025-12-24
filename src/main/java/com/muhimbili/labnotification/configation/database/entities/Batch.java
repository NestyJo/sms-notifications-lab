package com.muhimbili.labnotification.configation.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "batches",
        uniqueConstraints = @UniqueConstraint(name = "uq_batches_window",
                columnNames = {"results_date", "from_time", "to_time", "status"}))
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "results_date", nullable = false)
    private LocalDate resultsDate;

    @Column(name = "from_time", nullable = false)
    private LocalTime fromTime;

    @Column(name = "to_time", nullable = false)
    private LocalTime toTime;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getResultsDate() {
        return resultsDate;
    }

    public void setResultsDate(LocalDate resultsDate) {
        this.resultsDate = resultsDate;
    }

    public LocalTime getFromTime() {
        return fromTime;
    }

    public void setFromTime(LocalTime fromTime) {
        this.fromTime = fromTime;
    }

    public LocalTime getToTime() {
        return toTime;
    }

    public void setToTime(LocalTime toTime) {
        this.toTime = toTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }
}
