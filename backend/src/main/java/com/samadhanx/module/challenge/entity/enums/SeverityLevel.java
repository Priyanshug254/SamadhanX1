package com.samadhanx.module.challenge.entity.enums;

public enum SeverityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public int getWeight() {
        return switch (this) {
            case LOW -> 20;
            case MEDIUM -> 50;
            case HIGH -> 80;
            case CRITICAL -> 100;
        };
    }
}
