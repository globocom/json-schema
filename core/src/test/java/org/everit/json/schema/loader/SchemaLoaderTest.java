package org.everit.json.schema.loader;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import org.everit.json.schema.*;
import org.everit.json.schema.internal.*;
import org.everit.json.schema.loader.SchemaLoader.SchemaLoaderBuilder;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SchemaLoaderTest {

    private static JSONObject ALL_SCHEMAS;

    static {
        try {
            ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("testschemas.json");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject get(final String schemaName) throws Exception {
        return ALL_SCHEMAS.getJSONObject(schemaName);
    }

    private InputStream asStream(final String string) {
        return new ByteArrayInputStream(string.getBytes());
    }

    @Test
    public void booleanSchema() throws Exception {
        BooleanSchema actual = (BooleanSchema) SchemaLoader.load(get("booleanSchema"));
        Assert.assertNotNull(actual);
    }

    @Test
    public void builderhasDefaultFormatValidators() throws Exception {
        SchemaLoader actual = SchemaLoader.builder().schemaJson(get("booleanSchema")).build();
        assertTrue(actual.getFormatValidator("date-time").get() instanceof DateTimeFormatValidator);
        assertTrue(actual.getFormatValidator("uri").get() instanceof URIFormatValidator);
        assertTrue(actual.getFormatValidator("email").get() instanceof EmailFormatValidator);
        assertTrue(actual.getFormatValidator("ipv4").get() instanceof IPV4Validator);
        assertTrue(actual.getFormatValidator("ipv6").get() instanceof IPV6Validator);
        assertTrue(actual.getFormatValidator("hostname").get() instanceof HostnameFormatValidator);
    }

    @Test
    public void builderUsesDefaultSchemaClient() {
        SchemaLoaderBuilder actual = SchemaLoader.builder();
        Assert.assertNotNull(actual);
        assertTrue(actual.httpClient instanceof DefaultSchemaClient);
    }

    @Test
    public void customFormat() throws Exception {
        Schema subject = SchemaLoader.builder()
                .schemaJson(get("customFormat"))
                .addFormatValidator(new AbstractFormatValidator() {
                    @Override
                    public Optional<String> validate(String subject) {
                        return Optional.of("failure");
                    }

                    @Override
                    public String formatName() {
                        return "custom";
                    }
                })
                .build().load().build();
        TestSupport.expectFailure(subject, "asd");
    }

    @Test
    public void emptyPatternProperties() throws Exception {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("emptyPatternProperties"));
        Assert.assertNotNull(actual);
        assertEquals(0, actual.getPatternProperties().size());
    }

    @Test
    public void emptySchema() throws Exception {
        assertTrue(SchemaLoader.load(get("emptySchema")) instanceof EmptySchema);
    }

    @Test
    public void emptySchemaWithDefault() throws Exception {
        EmptySchema actual = (EmptySchema) SchemaLoader.load(get("emptySchemaWithDefault"));
        Assert.assertNotNull(actual);
    }

    @Test
    public void enumSchema() throws Exception {
        EnumSchema actual = (EnumSchema) SchemaLoader.load(get("enumSchema"));
        Assert.assertNotNull(actual);
        assertEquals(4, actual.getPossibleValues().size());
    }

    @Test
    public void genericProperties() throws Exception {
        Schema actual = SchemaLoader.load(get("genericProperties"));
        assertEquals("myId", actual.getId());
        assertEquals("my title", actual.getTitle());
        assertEquals("my description", actual.getDescription());
    }

    @Test
    public void integerSchema() throws Exception {
        NumberSchema actual = (NumberSchema) SchemaLoader.load(get("integerSchema"));
        assertEquals(10, actual.getMinimum().intValue());
        assertEquals(20, actual.getMaximum().intValue());
        assertEquals(5, actual.getMultipleOf().intValue());
        assertTrue(actual.isExclusiveMinimum());
        assertTrue(actual.isExclusiveMaximum());
        assertTrue(actual.requiresInteger());
    }

    @Test(expected = SchemaException.class)
    public void invalidExclusiveMinimum() throws Exception {
        SchemaLoader.load(get("invalidExclusiveMinimum"));
    }

    @Test(expected = SchemaException.class)
    public void invalidIntegerSchema() throws Exception {
        JSONObject input = get("invalidIntegerSchema");
        SchemaLoader.load(input);
    }

    @Test(expected = SchemaException.class)
    public void invalidStringSchema() throws Exception {
        SchemaLoader.load(get("invalidStringSchema"));
    }

    @Test(expected = SchemaException.class)
    public void invalidType() throws Exception {
        SchemaLoader.load(get("invalidType"));
    }

    @Test
    public void jsonPointerInArray() throws Exception {
        assertTrue(SchemaLoader.load(get("jsonPointerInArray")) instanceof ArraySchema);
    }

    @Test
    public void multipleTypes() throws Exception {
        assertTrue(SchemaLoader.load(get("multipleTypes")) instanceof CombinedSchema);
    }

    @Test
    public void implicitAnyOfLoadsTypeProps() throws Exception {
        CombinedSchema schema = (CombinedSchema) SchemaLoader.load(get("multipleTypesWithProps"));
        StringSchema stringSchema = FluentIterable.from(schema.getSubschemas())
                .firstMatch(Predicates.instanceOf(StringSchema.class))
                .transform(new Function<Schema, StringSchema>() {
                    @Override
                    public StringSchema apply(Schema input) {
                        return (StringSchema) input;
                    }
                })
                .or(new Supplier<StringSchema>() {
                    @Override
                    public StringSchema get() {
                        throw new AssertionError("no StringSchema");
                    }
                });
        NumberSchema numSchema = FluentIterable.from(schema.getSubschemas())
                .firstMatch(Predicates.instanceOf(NumberSchema.class))
                .transform(new Function<Schema, NumberSchema>() {
                    @Override
                    public NumberSchema apply(Schema input) {
                        return (NumberSchema) input;
                    }
                })
                .or(new Supplier<NumberSchema>() {
                    @Override
                    public NumberSchema get() {
                        throw new AssertionError("no NumberSchema");
                    }
                });
        assertEquals(3, stringSchema.getMinLength().intValue());
        assertEquals(5, numSchema.getMinimum().intValue());
    }

    @Test
    public void neverMatchingAnyOf() throws Exception {
        assertTrue(SchemaLoader.load(get("anyOfNeverMatches")) instanceof CombinedSchema);
    }

    @Test
    public void noExplicitObject() throws Exception {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("noExplicitObject"));
        Assert.assertFalse(actual.requiresObject());
    }

    @Test
    public void notSchema() throws Exception {
        NotSchema actual = (NotSchema) SchemaLoader.load(get("notSchema"));
        Assert.assertNotNull(actual);
    }

    @Test
    public void nullSchema() throws Exception {
        NullSchema actual = (NullSchema) SchemaLoader.load(get("nullSchema"));
        Assert.assertNotNull(actual);
    }

    @Test
    public void pointerResolution() throws Exception {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("pointerResolution"));
        ObjectSchema rectangleSchema = (ObjectSchema) ((ReferenceSchema) actual.getPropertySchemas()
                .get("rectangle"))
                .getReferredSchema();
        Assert.assertNotNull(rectangleSchema);
        ReferenceSchema aRef = (ReferenceSchema) rectangleSchema.getPropertySchemas().get("a");
        assertTrue(aRef.getReferredSchema() instanceof NumberSchema);
    }

    @Test(expected = SchemaException.class)
    public void pointerResolutionFailure() throws Exception {
        SchemaLoader.load(get("pointerResolutionFailure"));
    }

    @Test(expected = SchemaException.class)
    public void pointerResolutionQueryFailure() throws Exception {
        SchemaLoader.load(get("pointerResolutionQueryFailure"));
    }

    @Test
    public void propsAroundRefExtendTheReferredSchema() throws Exception {
        ObjectSchema actual = (ObjectSchema) SchemaLoader
                .load(get("propsAroundRefExtendTheReferredSchema"));
        ObjectSchema prop = (ObjectSchema) ((ReferenceSchema) actual.getPropertySchemas().get("prop"))
                .getReferredSchema();
        assertTrue(prop.requiresObject());
        assertEquals(1, prop.getMinProperties().intValue());
    }

    @Test
    public void recursiveSchema() throws Exception {
        SchemaLoader.load(get("recursiveSchema"));
    }

    @Test
    public void refWithType() throws Exception {
        ObjectSchema actualRoot = (ObjectSchema) SchemaLoader.load(get("refWithType"));
        ReferenceSchema actual = (ReferenceSchema) actualRoot.getPropertySchemas().get("prop");
        ObjectSchema propSchema = (ObjectSchema) actual.getReferredSchema();
        assertEquals(propSchema.getRequiredProperties(), Arrays.asList("a", "b"));
    }

    @Test
    public void remotePointerResulion() throws Exception {
        SchemaClient httpClient = Mockito.mock(SchemaClient.class);
        Mockito.when(httpClient.get("http://example.org/asd")).thenReturn(asStream("{}"));
        Mockito.when(httpClient.get("http://example.org/otherschema.json")).thenReturn(asStream("{}"));
        Mockito.when(httpClient.get("http://example.org/folder/subschemaInFolder.json")).thenReturn(
                asStream("{}"));
        SchemaLoader.load(get("remotePointerResolution"), httpClient);
    }

    @Test
    public void resolutionScopeTest() throws Exception {
        SchemaLoader.load(get("resolutionScopeTest"), new SchemaClient() {

            @Override
            public InputStream get(final String url) {
                System.out.println("GET " + url);
                return new DefaultSchemaClient().get(url);
            }
        });
    }

    @Test
    public void selfRecursiveSchema() throws Exception {
        SchemaLoader.load(get("selfRecursiveSchema"));
    }

    @Test
    public void sniffByFormat() throws Exception {
        JSONObject schema = new JSONObject();
        schema.put("format", "hostname");
        Schema actual = SchemaLoader.builder().schemaJson(schema).build().load().build();
        assertTrue(actual instanceof StringSchema);
    }

    @Test
    public void stringSchema() throws Exception {
        StringSchema actual = (StringSchema) SchemaLoader.load(get("stringSchema"));
        assertEquals(2, actual.getMinLength().intValue());
        assertEquals(3, actual.getMaxLength().intValue());
    }

    @Test
    public void stringSchemaWithFormat() throws Exception {
        StringSchema subject = (StringSchema) SchemaLoader.load(get("stringSchemaWithFormat"));
        TestSupport.expectFailure(subject, "asd");
    }

    @Test
    public void tupleSchema() throws Exception {
        ArraySchema actual = (ArraySchema) SchemaLoader.load(get("tupleSchema"));
        Assert.assertFalse(actual.permitsAdditionalItems());
        Assert.assertNull(actual.getAllItemSchema());
        assertEquals(2, actual.getItemSchemas().size());
        assertEquals(BooleanSchema.INSTANCE, actual.getItemSchemas().get(0));
        assertEquals(NullSchema.INSTANCE, actual.getItemSchemas().get(1));
    }

    @Test(expected = SchemaException.class)
    public void unknownSchema() throws Exception {
        SchemaLoader.load(get("unknown"));
    }

    @Test
    public void unsupportedFormat() throws Exception {
        JSONObject schema = new JSONObject();
        schema.put("type", "string");
        schema.put("format", "unknown");
        SchemaLoader.builder().schemaJson(schema).build().load();
    }

    @Test
    public void schemaJsonIdIsRecognized() throws Exception {
        SchemaClient client = Mockito.mock(SchemaClient.class);
        ByteArrayInputStream retval = new ByteArrayInputStream("{}".getBytes());
        Mockito.when(client.get("http://example.org/schema/schema.json")).thenReturn(retval);
        SchemaLoader.builder().schemaJson(get("schemaWithId"))
                .httpClient(client)
                .build().load();
    }

    @Test
    public void withoutFragment() {
        String actual = ReferenceLookup.withoutFragment("http://example.com#frag").toString();
        assertEquals("http://example.com", actual);
    }

    @Test
    public void withoutFragmentNoFragment() {
        String actual = ReferenceLookup.withoutFragment("http://example.com").toString();
        assertEquals("http://example.com", actual);
    }

}
