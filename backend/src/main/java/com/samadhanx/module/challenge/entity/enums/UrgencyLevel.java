package com.samadhanx.module.challenge.entity.enums;

public enum UrgencyLevel {
    LOW,
    MEDIUM,
    HIGH,
    IMMEDIATE;

    public int getWeight() {
        return switch (this) {
            case LOW -> 20;
            case MEDIUM -> 50;
            case HIGH -> 80;
            case IMMEDIATE -> 100;
        };
    }
}
