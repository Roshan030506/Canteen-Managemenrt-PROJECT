package com.canteen.web;

import com.canteen.model.Customer;
import com.canteen.model.MenuItem;
import com.canteen.model.Order;
import com.canteen.model.OrderRequest;
import com.canteen.service.CanteenService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServer {
    private final int port;
    private final CanteenService canteenService;
    private HttpServer server;

    public WebServer(int port, CanteenService canteenService) {
        this.port = port;
        this.canteenService = canteenService;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Static assets handler
        server.createContext("/", new StaticHandler());
        server.createContext("/index.html", new StaticHandler());
        
        // REST API routes
        server.createContext("/api/stats", new StatsHandler());
        server.createContext("/api/menu", new MenuHandler());
        server.createContext("/api/customers", new CustomersHandler());
        server.createContext("/api/orders", new OrdersHandler());

        server.setExecutor(null); // default executor
        server.start();
        System.out.println("Web Server successfully running at http://localhost:" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // =======================================================
    // STATIC RESOURCE HANDLER
    // =======================================================
    private static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] responseBytes = null;
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/") || path.equals("/index.html")) {
                // Try to load from classpath resources first
                try (InputStream is = WebServer.class.getResourceAsStream("/web/index.html")) {
                    if (is != null) {
                        responseBytes = readAllBytes(is);
                    }
                }
                
                // Fallback to loading directly from the filesystem during development
                if (responseBytes == null) {
                    try {
                        responseBytes = Files.readAllBytes(Paths.get("src/main/resources/web/index.html"));
                    } catch (IOException ignored) {
                        // ignore and try next fallback
                    }
                }
            }

            if (responseBytes == null) {
                // Return simple 404
                String notFound = "Resource not found";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes());
                }
                return;
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    // =======================================================
    // REST API HANDLERS
    // =======================================================

    // GET /api/stats
    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            try {
                int totalOrders = canteenService.getTotalOrders();
                double totalRevenue = canteenService.getTotalRevenue();
                MenuItem topItem = canteenService.getTopSellingItem();
                String topItemName = topItem != null ? topItem.getName() : "N/A";

                String json = String.format("{\"totalOrders\":%d,\"totalRevenue\":%.2f,\"topSellingItem\":\"%s\"}",
                        totalOrders, totalRevenue, escapeJson(topItemName));
                sendJsonResponse(exchange, 200, json);
            } catch (SQLException e) {
                sendError(exchange, 500, "Database Error: " + e.getMessage());
            }
        }
    }

    // GET/POST /api/menu
    private class MenuHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    List<MenuItem> items = canteenService.getAllMenuItems();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < items.size(); i++) {
                        MenuItem item = items.get(i);
                        sb.append(String.format("{\"id\":%d,\"name\":\"%s\",\"category\":\"%s\",\"price\":%.2f,\"quantity\":%d}",
                                item.getId(), escapeJson(item.getName()), escapeJson(item.getCategory()), item.getPrice(), item.getQuantity()));
                        if (i < items.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJsonResponse(exchange, 200, sb.toString());
                } catch (SQLException e) {
                    sendError(exchange, 500, "Database Error: " + e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                try {
                    String body = readStream(exchange.getRequestBody());
                    String name = parseJsonStringField(body, "name");
                    String category = parseJsonStringField(body, "category");
                    double price = parseJsonDoubleField(body, "price");
                    int quantity = parseJsonIntField(body, "quantity");

                    canteenService.addMenuItem(name, category, price, quantity);
                    sendJsonResponse(exchange, 201, "{\"status\":\"success\"}");
                } catch (IllegalArgumentException e) {
                    sendError(exchange, 400, e.getMessage());
                } catch (SQLException e) {
                    sendError(exchange, 500, "Database Error: " + e.getMessage());
                }
            } else {
                sendError(exchange, 405, "Method Not Allowed");
            }
        }
    }

    // GET/POST /api/customers
    private class CustomersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    List<Customer> customers = canteenService.getAllCustomers();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < customers.size(); i++) {
                        Customer c = customers.get(i);
                        sb.append(String.format("{\"id\":%d,\"name\":\"%s\",\"phone\":\"%s\"}",
                                c.getId(), escapeJson(c.getName()), escapeJson(c.getPhone())));
                        if (i < customers.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJsonResponse(exchange, 200, sb.toString());
                } catch (SQLException e) {
                    sendError(exchange, 500, "Database Error: " + e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                try {
                    String body = readStream(exchange.getRequestBody());
                    String name = parseJsonStringField(body, "name");
                    String phone = parseJsonStringField(body, "phone");

                    canteenService.registerCustomer(name, phone);
                    sendJsonResponse(exchange, 201, "{\"status\":\"success\"}");
                } catch (IllegalArgumentException e) {
                    sendError(exchange, 400, e.getMessage());
                } catch (SQLException e) {
                    sendError(exchange, 500, "Database Error: " + e.getMessage());
                }
            } else {
                sendError(exchange, 405, "Method Not Allowed");
            }
        }
    }

    // GET/POST /api/orders
    private class OrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                try {
                    List<Order> orders = canteenService.getAllOrders();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < orders.size(); i++) {
                        Order o = orders.get(i);
                        sb.append(String.format("{\"id\":%d,\"customerName\":\"%s\",\"totalAmount\":%.2f,\"orderTime\":\"%s\",\"itemLines\":[",
                                o.getId(), escapeJson(o.getCustomerName()), o.getTotalAmount(), o.getOrderTime()));
                        List<String> lines = o.getItemLines();
                        for (int j = 0; j < lines.size(); j++) {
                            sb.append("\"").append(escapeJson(lines.get(j))).append("\"");
                            if (j < lines.size() - 1) sb.append(",");
                        }
                        sb.append("]}");
                        if (i < orders.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJsonResponse(exchange, 200, sb.toString());
                } catch (SQLException e) {
                    sendError(exchange, 500, "Database Error: " + e.getMessage());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                try {
                    String body = readStream(exchange.getRequestBody());
                    
                    // Parse Customer ID
                    int customerId = parseJsonIntField(body, "customerId");

                    // Parse Order Items Array
                    List<OrderRequest> requests = new ArrayList<>();
                    Pattern itemPattern = Pattern.compile("\\{\\s*\"menuItemId\"\\s*:\\s*(\\d+)\\s*,\\s*\"quantity\"\\s*:\\s*(\\d+)\\s*\\}");
                    Matcher matcher = itemPattern.matcher(body);
                    while (matcher.find()) {
                        int menuItemId = Integer.parseInt(matcher.group(1));
                        int quantity = Integer.parseInt(matcher.group(2));
                        requests.add(new OrderRequest(menuItemId, quantity));
                    }

                    if (requests.isEmpty()) {
                        throw new IllegalArgumentException("Cart contains no valid menu items.");
                    }

                    canteenService.placeOrder(customerId, requests);
                    sendJsonResponse(exchange, 201, "{\"status\":\"success\"}");
                } catch (IllegalArgumentException e) {
                    sendError(exchange, 400, e.getMessage());
                } catch (SQLException e) {
                    sendError(exchange, 500, "Database Error: " + e.getMessage());
                }
            } else {
                sendError(exchange, 405, "Method Not Allowed");
            }
        }
    }

    // =======================================================
    // HELPER RESPONSE UTILITIES
    // =======================================================
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // =======================================================
    // LIGHTWEIGHT CUSTOM JSON REGEX PARSERS
    // =======================================================
    private static String parseJsonStringField(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"[\\s]*:[\\s]*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Missing or invalid string field: " + fieldName);
    }

    private static int parseJsonIntField(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"[\\s]*:[\\s]*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("Missing or invalid numeric field: " + fieldName);
    }

    private static double parseJsonDoubleField(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"[\\s]*:[\\s]*([\\d\\.]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        throw new IllegalArgumentException("Missing or invalid double field: " + fieldName);
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String readStream(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
