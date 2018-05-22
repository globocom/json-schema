package org.everit.json.schema.loader;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author erosb
 */
public class CombinedSchemaLoaderTest {

    private static JSONObject ALL_SCHEMAS;

    static {
        try {
            ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("combinedtestschemas.json");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject get(final String schemaName) throws JSONException {
        return ALL_SCHEMAS.getJSONObject(schemaName);
    }

    @Test
    public void combinedSchemaLoading() throws Exception {
        CombinedSchema actual = (CombinedSchema) SchemaLoader.load(get("combinedSchema"));
        Assert.assertNotNull(actual);
    }

    @Test
    public void combinedSchemaWithBaseSchema() throws Exception {
        CombinedSchema actual = (CombinedSchema) SchemaLoader.load(get("combinedSchemaWithBaseSchema"));
        Assert.assertEquals(1, FluentIterable.from(actual.getSubschemas())
                .filter(Predicates.instanceOf(StringSchema.class)).size());
        Assert.assertEquals(1, FluentIterable.from(actual.getSubschemas())
                .filter(Predicates.instanceOf(CombinedSchema.class)).size());
    }

    @Test
    public void combinedSchemaWithExplicitBaseSchema() throws Exception {
        CombinedSchema actual = (CombinedSchema) SchemaLoader
                .load(get("combinedSchemaWithExplicitBaseSchema"));
        Assert.assertEquals(1, FluentIterable.from(actual.getSubschemas())
                .filter(Predicates.instanceOf(StringSchema.class)).size());
        Assert.assertEquals(1, FluentIterable.from(actual.getSubschemas())
                .filter(Predicates.instanceOf(CombinedSchema.class)).size());
    }

    @Test
    public void combinedSchemaWithMultipleBaseSchemas() throws Exception {
        Schema actual = SchemaLoader.load(get("combinedSchemaWithMultipleBaseSchemas"));
        assertTrue(actual instanceof CombinedSchema);
    }

}
