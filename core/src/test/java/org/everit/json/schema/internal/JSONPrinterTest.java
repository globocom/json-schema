package org.everit.json.schema.internal;

import org.everit.json.schema.NullSchema;
import org.everit.json.schema.Schema;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JSONPrinterTest {

    private StringWriter buffer;

    @Before
    public void before() {
        buffer = new StringWriter();
    }

    private JSONObject actualObj() throws JSONException {
        return new JSONObject(buffer.toString());
    }

    @Test
    public void constructor() {
        new JSONPrinter(new StringWriter());
    }

    private JSONPrinter subject() {
        return new JSONPrinter(buffer);
    }

    @Test
    public void keyValueDelegates() throws JSONException {
        JSONPrinter subject = subject();
        subject.object();
        subject.key("mykey");
        subject.value("myvalue");
        subject.endObject();
        assertEquals("myvalue", actualObj().get("mykey"));
    }

    @Test
    public void ifPresentPrints() throws JSONException {
        JSONPrinter subject = subject();
        subject.object();
        subject.ifPresent("mykey", "myvalue");
        subject.endObject();
        assertEquals("myvalue", actualObj().get("mykey"));
    }

    @Test
    public void ifPresentOmits() throws JSONException {
        JSONPrinter subject = subject();
        subject.object();
        subject.ifPresent("mykey", null);
        subject.endObject();
        assertNull(actualObj().opt("mykey"));
    }

    @Test
    public void ifTruePints() throws JSONException {
        JSONPrinter subject = subject();
        subject.object();
        subject.ifTrue("uniqueItems", true);
        subject.endObject();
        assertEquals(true, actualObj().getBoolean("uniqueItems"));
    }

    @Test
    public void ifTrueHandlesNullAsFalse() throws JSONException {
        JSONPrinter subject = subject();
        subject.object();
        subject.ifTrue("uniqueItems", null);
        subject.endObject();
        assertNull(actualObj().opt("uniqueItems"));
    }

    @Test
    public void ifTrueOmits() throws JSONException {
        JSONPrinter subject = subject();
        subject.object();
        subject.ifTrue("uniqueItems", false);
        subject.endObject();
        assertNull(actualObj().opt("uniqueItems"));
    }

    @Test
    public void ifFalsePrints() throws JSONException {
        JSONPrinter subject = subject();
        subject.object();
        subject.ifFalse("mykey", false);
        subject.endObject();
        assertEquals(false, actualObj().getBoolean("mykey"));
    }

    @Test
    public void ifFalseOmits() throws JSONException {
        JSONPrinter subject = subject();
        subject.object();
        subject.ifFalse("mykey", true);
        subject.endObject();
        assertNull(actualObj().opt("mykey"));
    }

    @Test
    public void ifFalseHandlesNullAsTrue() throws JSONException {
        JSONPrinter subject = subject();
        subject.object();
        subject.ifFalse("mykey", null);
        subject.endObject();
        assertNull(actualObj().opt("mykey"));
    }

    @Test
    public void arraySupport() throws Exception {
        JSONPrinter subject = subject();
        subject.array();
        subject.value(true);
        subject.endArray();
        assertEquals("[true]", buffer.toString());
    }

    @Test
    public void printSchemaMap() throws Exception {
        HashMap<Number, Schema> input = new HashMap<Number, Schema>();
        input.put(2, NullSchema.INSTANCE);
        subject().printSchemaMap(input);
        assertEquals("{\"2\":" + NullSchema.INSTANCE.toString() + "}", buffer.toString());
    }

}
