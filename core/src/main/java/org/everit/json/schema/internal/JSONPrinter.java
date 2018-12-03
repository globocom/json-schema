package org.everit.json.schema.internal;

import org.everit.json.schema.Schema;
import org.json.JSONException;

import java.io.Writer;
import java.util.Map;

public class JSONPrinter {

    private final JSONWriter writer;

    public JSONPrinter(final Writer writer) {
        if (writer == null) {
            throw new NullPointerException("writer cannot be null");
        }
        this.writer = new JSONWriter(writer);
    }

    public JSONPrinter key(final String key) throws JSONException {
        writer.key(key);
        return this;
    }

    public JSONPrinter value(final Object value) throws JSONException {
        writer.value(value);
        return this;
    }

    public JSONPrinter object() throws JSONException {
        writer.object();
        return this;
    }

    public JSONPrinter endObject() throws JSONException {
        writer.endObject();
        return this;
    }

    public JSONPrinter ifPresent(final String key, final Object value) throws JSONException {
        if (value != null) {
            key(key);
            value(value);
        }
        return this;
    }

    public JSONPrinter ifTrue(final String key, final Boolean value) throws JSONException {
        if (value != null && value) {
            key(key);
            value(value);
        }
        return this;
    }

    public JSONPrinter array() throws JSONException {
        writer.array();
        return this;
    }

    public JSONPrinter endArray() throws JSONException {
        writer.endArray();
        return this;
    }

    public void ifFalse(String key, Boolean value) throws JSONException {
        if (value != null && !value) {
            writer.key(key);
            writer.value(value);
        }
    }

    public <K> void printSchemaMap(Map<K, Schema> input) throws JSONException {
        object();

        for (Map.Entry<K, Schema> entry : input.entrySet()) {
            key(entry.getKey().toString());
            entry.getValue().describeTo(this);
        }

        endObject();
    }
}
