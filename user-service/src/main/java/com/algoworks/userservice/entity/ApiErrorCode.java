package com.algoworks.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "api_error_codes")
public class ApiErrorCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "error_code", nullable = false, unique = true, length = 20)
    private String errorCode;

    @Column(name = "service_name", nullable = false, length = 50)
    private String serviceName;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "http_status", nullable = false)
    private int httpStatus;

    @Column(nullable = false)
    private boolean active = true;

    public ApiErrorCode() {
    }

    public Long getId() {
        return id;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDescription() {
        return description;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public boolean isActive() {
        return active;
    }
}

