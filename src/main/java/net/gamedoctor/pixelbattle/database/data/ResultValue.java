package net.gamedoctor.pixelbattle.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ResultValue {
    @Getter
    private final String key;
    private final Object value;

    public String getValueASString() {
        return (String) value;
    }

    public int getValueASInteger() {
        return ((Number) value).intValue();
    }

    public double getValueASDouble() {
        return ((Number) value).doubleValue();
    }

    public long getValueASLong() {
        return ((Number) value).longValue();
    }
}
