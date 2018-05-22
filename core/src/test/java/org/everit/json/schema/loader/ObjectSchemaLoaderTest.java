package org.everit.json.schema.loader;

import org.everit.json.schema.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class ObjectSchemaLoaderTest {

    private static JSONObject ALL_SCHEMAS;

    static {
        try {
            ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("objecttestschemas.json");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject get(final String schemaName) throws JSONException {
        return ALL_SCHEMAS.getJSONObject(schemaName);
    }

    @Test
    public void objectSchema() throws Exception {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectSchema"));
        Assert.assertNotNull(actual);
        Map<String, Schema> propertySchemas = actual.getPropertySchemas();
        assertEquals(2, propertySchemas.size());
        assertEquals(BooleanSchema.INSTANCE, propertySchemas.get("boolProp"));
        Assert.assertFalse(actual.permitsAdditionalProperties());
        assertEquals(2, actual.getRequiredProperties().size());
        assertEquals(2, actual.getMinProperties().intValue());
        assertEquals(3, actual.getMaxProperties().intValue());
    }

    @Test(expected = SchemaException.class)
    public void objectInvalidAdditionalProperties() throws Exception {
        SchemaLoader.load(get("objectInvalidAdditionalProperties"));
    }

    @Test
    public void objectWithAdditionalPropSchema() throws Exception {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithAdditionalPropSchema"));
        assertEquals(BooleanSchema.INSTANCE, actual.getSchemaOfAdditionalProperties());
    }

    @Test
    public void objectWithPropDep() throws Exception {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithPropDep"));
        assertEquals(1, actual.getPropertyDependencies().get("isIndividual").size());
    }

    @Test
    public void objectWithSchemaDep() throws Exception {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithSchemaDep"));
        assertEquals(1, actual.getSchemaDependencies().size());
    }

    @Test
    public void patternProperties() throws Exception {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("patternProperties"));
        Assert.assertNotNull(actual);
        assertEquals(2, actual.getPatternProperties().size());
    }

    @Test(expected = SchemaException.class)
    public void invalidDependency() throws Exception {
        SchemaLoader.load(get("invalidDependency"));
    }


}
