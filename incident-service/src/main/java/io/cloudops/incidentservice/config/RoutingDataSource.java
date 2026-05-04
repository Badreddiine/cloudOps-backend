package io.cloudops.incidentservice.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> context = new ThreadLocal<>();

    public static void setDataSource(String key) {
        context.set(key);
    }

    public static void clear() {
        context.remove();
    }

    public static String current() {
        return context.get() != null ? context.get() : "eu";
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return current();
    }
}