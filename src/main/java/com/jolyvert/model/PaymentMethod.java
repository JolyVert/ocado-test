package com.jolyvert.model;

import java.math.BigDecimal;

// Represents a payment method with an ID, discount percentage, and spending limit
public record PaymentMethod(String id, int discount, BigDecimal limit) {}
