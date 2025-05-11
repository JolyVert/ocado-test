package com.jolyvert.model;

import java.math.BigDecimal;
import java.util.List;

// Represents a customer order with an ID, value, and applicable promotions
public record Order(String id, BigDecimal value, List<String> promotions) {}

