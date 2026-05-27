package com.marcosmoreira.mitiendadebarrio.admin.shared.result;

/** Códigos de error locales. No son códigos HTTP. */
public enum AppErrorCode {
    VALIDATION_ERROR,
    NOT_FOUND,
    CONFLICT,
    BUSINESS_RULE_VIOLATION,
    DATABASE_ERROR,
    LICENSE_EXPIRED,
    BACKUP_ERROR,
    UNKNOWN_ERROR
}
