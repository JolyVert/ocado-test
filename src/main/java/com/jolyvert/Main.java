package com.jolyvert;

import com.jolyvert.model.Order;
import com.jolyvert.model.PaymentMethod;
import com.jolyvert.service.JsonLoader;
import com.jolyvert.service.PaymentOptimizer;

import java.util.List;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java -jar app.jar <orders.json> <paymentmethods.json>");
            return;
        }

        List<Order> orders = JsonLoader.loadOrders(args[0]);
        Map<String, PaymentMethod> methods = JsonLoader.loadPaymentMethods(args[1]);

        Map<String, BigDecimal> result = new PaymentOptimizer().optimizePayments(orders, methods);

        result.forEach((k, v) ->
                System.out.println(k + " " + v.setScale(2, RoundingMode.HALF_UP)));
    }
}
