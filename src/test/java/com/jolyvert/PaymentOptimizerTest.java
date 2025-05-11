package com.jolyvert;

import com.jolyvert.model.Order;
import com.jolyvert.model.PaymentMethod;
import com.jolyvert.service.PaymentOptimizer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentOptimizerTest {

    @Test
    public void testExampleScenario() {
        // Scenario with mixed promotional cards and points

        List<Order> orders = new ArrayList<>();
        // Order 1: 100 with mZysk (10%)
        orders.add(new Order("ORDER1", new BigDecimal("100.00"), List.of("mZysk")));
        // Order 2: 200 with BosBankrut (5%)
        orders.add(new Order("ORDER2", new BigDecimal("200.00"), List.of("BosBankrut")));
        // Order 3: 150 with mZysk (10%) and BosBankrut (5%) – choose the cheaper
        orders.add(new Order("ORDER3", new BigDecimal("150.00"), List.of("mZysk", "BosBankrut")));
        // Order 4: 50 with no promotion – fallback to points or card
        orders.add(new Order("ORDER4", new BigDecimal("50.00"), List.of()));

        Map<String, PaymentMethod> methods = new HashMap<>();
        // Points: 15% discount, 100 limit
        methods.put("PUNKTY", new PaymentMethod("PUNKTY", 15, new BigDecimal("100.00")));
        // mZysk: 10% discount, 180 limit
        methods.put("mZysk", new PaymentMethod("mZysk", 10, new BigDecimal("180.00")));
        // BosBankrut: 5% discount, 200 limit
        methods.put("BosBankrut", new PaymentMethod("BosBankrut", 5, new BigDecimal("200.00")));

        Map<String, BigDecimal> result = new PaymentOptimizer().optimizePayments(orders, methods);

        // Validate expected payments:
        assertEquals(new BigDecimal("165.00"), result.get("mZysk"), "mZysk payment should be 165.00");
        assertEquals(new BigDecimal("190.00"), result.get("BosBankrut"), "BosBankrut payment should be 190.00");
        assertEquals(new BigDecimal("100.00"), result.get("PUNKTY"), "Points payment should be 100.00");
    }

    @Test
    public void testPointsOnlyScenario() {
        // Scenario where only points are available and sufficient for both orders

        List<Order> orders = new ArrayList<>();
        orders.add(new Order("O1", new BigDecimal("50.00"), null));
        orders.add(new Order("O2", new BigDecimal("50.00"), null));

        Map<String, PaymentMethod> methods = new HashMap<>();
        // Points with 10% discount, enough balance to cover both orders
        methods.put("PUNKTY", new PaymentMethod("PUNKTY", 10, new BigDecimal("100.00")));

        Map<String, BigDecimal> result = new PaymentOptimizer().optimizePayments(orders, methods);

        // Each order gets 10% discount: 45.00 each => total 90.00 points used
        assertEquals(new BigDecimal("90.00"), result.get("PUNKTY"), "Total points payment should be 90.00");
    }

    @Test
    public void testPartialPointsNotEnoughForFull() {
        // Points are insufficient to fully cover the order, fallback card is used

        List<Order> orders = new ArrayList<>();
        orders.add(new Order("O3", new BigDecimal("100.00"), List.of()));

        Map<String, PaymentMethod> methods = new HashMap<>();
        // Only 5.00 in points available, but need at least 10.00 for 10% mixed payment
        methods.put("PUNKTY", new PaymentMethod("PUNKTY", 10, new BigDecimal("5.00")));
        // CARD1 has no discount, but enough balance
        methods.put("CARD1", new PaymentMethod("CARD1", 0, new BigDecimal("100.00")));

        Map<String, BigDecimal> result = new PaymentOptimizer().optimizePayments(orders, methods);

        // Points can't be used; entire payment is made by CARD1
        assertEquals(new BigDecimal("0"), result.get("PUNKTY"), "No points used");
        assertEquals(new BigDecimal("100.00"), result.get("CARD1"), "Card should pay full amount");
    }
}



