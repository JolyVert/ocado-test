package com.jolyvert.service;

import com.jolyvert.model.Order;
import com.jolyvert.model.PaymentMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class PaymentOptimizer {
    private static final String POINTS = "PUNKTY";

    public Map<String, BigDecimal> optimizePayments(List<Order> orders, Map<String, PaymentMethod> methods) {
        // 1) Initialize the state: zero spent, full balance per method
        Map<String, BigDecimal> spent = new HashMap<>();
        Map<String, BigDecimal> balance = new HashMap<>();
        for (var entry : methods.entrySet()) {
            spent.put(entry.getKey(), BigDecimal.ZERO);
            balance.put(entry.getKey(), entry.getValue().limit());
        }

        // 2) Sort orders by the highest possible card savings and try to allocate them
        List<Order> remaining = new ArrayList<>();
        orders.sort((a, b) -> maxCardSavings(b, methods).compareTo(maxCardSavings(a, methods)));
        for (Order o : orders) {
            String bestCard = findBestCard(o, methods, balance);
            if (bestCard != null) {
                BigDecimal cost = applyDiscount(o.value(), methods.get(bestCard).discount());
                pay(bestCard, cost, spent, balance);
            } else {
                remaining.add(o);
            }
        }

        // 3) Try to fully pay remaining orders with points (15% discount)
        List<Order> next = new ArrayList<>();
        for (Order o : remaining) {
            BigDecimal fullCost = applyDiscount(o.value(), methods.get(POINTS).discount());
            if (balance.get(POINTS).compareTo(fullCost) >= 0) {
                pay(POINTS, fullCost, spent, balance);
            } else {
                next.add(o);
            }
        }

        // 4) Mixed payment: 10% points + 90% fallback card
        for (Order o : next) {
            BigDecimal tenPct = round(o.value().multiply(BigDecimal.valueOf(0.10)));
            BigDecimal ninetyCost = round(o.value().multiply(BigDecimal.valueOf(0.90)));
            BigDecimal pts = balance.get(POINTS);

            boolean paid = false;
            if (pts.compareTo(tenPct) >= 0) {
                BigDecimal usePts = pts.min(ninetyCost);
                BigDecimal rem = ninetyCost.subtract(usePts);
                String fb = findFallback(rem, methods, balance);
                if (fb != null) {
                    pay(POINTS, usePts, spent, balance);
                    pay(fb, rem, spent, balance);
                    paid = true;
                }
            }
            if (!paid) {
                // Fallback: try promotional card or full price card payment
                String card = findBestCard(o, methods, balance);
                if (card != null) {
                    BigDecimal cost = applyDiscount(o.value(), methods.get(card).discount());
                    pay(card, cost, spent, balance);
                } else {
                    String fb = findFallback(o.value(), methods, balance);
                    if (fb != null) {
                        pay(fb, o.value(), spent, balance);
                    }
                }
            }
        }
        return spent;
    }

    // Calculate the maximum possible card savings for an order
    private BigDecimal maxCardSavings(Order o, Map<String, PaymentMethod> methods) {
        BigDecimal max = BigDecimal.ZERO;
        if (o.promotions() != null) {
            for (String id : o.promotions()) {
                PaymentMethod m = methods.get(id);
                if (m != null) {
                    BigDecimal save = round(o.value().multiply(BigDecimal.valueOf(m.discount() / 100.0)));
                    if (save.compareTo(max) > 0) max = save;
                }
            }
        }
        return max;
    }

    // Find the best promotional card that can fully pay for the order with available balance
    private String findBestCard(Order o, Map<String, PaymentMethod> methods, Map<String, BigDecimal> balance) {
        String bestId = null;
        BigDecimal bestCost = null;
        if (o.promotions() != null) {
            for (String id : o.promotions()) {
                PaymentMethod m = methods.get(id);
                if (m == null) continue;
                BigDecimal cost = applyDiscount(o.value(), m.discount());
                if (balance.getOrDefault(id, BigDecimal.ZERO).compareTo(cost) >= 0 &&
                        (bestCost == null || cost.compareTo(bestCost) < 0)) {
                    bestCost = cost;
                    bestId = id;
                }
            }
        }
        return bestId;
    }

    // Find a fallback card that can cover the given amount (without discount)
    private String findFallback(BigDecimal amt, Map<String, PaymentMethod> methods, Map<String, BigDecimal> balance) {
        for (String id : methods.keySet()) {
            if (POINTS.equals(id)) continue;
            if (balance.getOrDefault(id, BigDecimal.ZERO).compareTo(amt) >= 0) return id;
        }
        return null;
    }

    // Apply discount to the value (percentage)
    private BigDecimal applyDiscount(BigDecimal v, int pct) {
        return round(v.multiply(BigDecimal.valueOf(1 - pct / 100.0)));
    }

    // Update spent and balance for a given method
    private void pay(String id, BigDecimal amt, Map<String, BigDecimal> spent, Map<String, BigDecimal> balance) {
        spent.put(id, spent.get(id).add(amt));
        balance.put(id, balance.get(id).subtract(amt));
    }

    // Round the value to 2 decimal places
    private BigDecimal round(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}

