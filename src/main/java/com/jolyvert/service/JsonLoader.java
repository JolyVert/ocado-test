package com.jolyvert.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jolyvert.model.Order;
import com.jolyvert.model.PaymentMethod;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Utility class responsible for loading JSON input files into model objects
public class JsonLoader {
    public static List<Order> loadOrders(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(path), new TypeReference<>() {});
    }

    public static Map<String, PaymentMethod> loadPaymentMethods(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> rawList = mapper.readValue(new File(path), new TypeReference<>() {});
        Map<String, PaymentMethod> result = new HashMap<>();
        for (Map<String, String> raw : rawList) {
            String id = raw.get("id");
            int discount = Integer.parseInt(raw.get("discount"));
            BigDecimal limit = new BigDecimal(raw.get("limit"));
            result.put(id, new PaymentMethod(id, discount, limit));
        }
        return result;
    }
}
