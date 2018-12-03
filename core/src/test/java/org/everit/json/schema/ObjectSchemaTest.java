/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import static org.junit.Assert.assertTrue;

public class ObjectSchemaTest {

    private JSONObject OBJECTS;
    private ResourceLoader loader = ResourceLoader.DEFAULT;

    @Before
    public void setup() throws JSONException {
        OBJECTS = loader.readObj("objecttestcases.json");
    }

    @Test
    public void additionalPropertiesOnEmptyObject() throws Exception {
        ObjectSchema.builder()
                .schemaOfAdditionalProperties(BooleanSchema.INSTANCE).build()
                .validate(OBJECTS.getJSONObject("emptyObject"));
    }

    @Test
    public void additionalPropertySchema() throws Exception {
        ObjectSchema subject = ObjectSchema.builder()
                .schemaOfAdditionalProperties(BooleanSchema.INSTANCE)
                .build();
        TestSupport.failureOf(subject)
                .input(OBJECTS.get("additionalPropertySchema"))
                .expectedPointer("#/foo")
                .expect();
        TestSupport.expectFailure(subject, "#/foo", OBJECTS.get("additionalPropertySchema"));
    }

    @Test
    public void maxPropertiesFailure() throws Exception {
        ObjectSchema subject = ObjectSchema.builder().maxProperties(2).build();
        TestSupport.failureOf(subject)
                .input(OBJECTS.get("maxPropertiesFailure"))
                .expectedPointer("#")
                .expectedKeyword("maxProperties")
                .expect();
    }

    @Test
    public void minPropertiesFailure() throws Exception {
        ObjectSchema subject = ObjectSchema.builder().minProperties(2).build();
        TestSupport.failureOf(subject)
                .input(OBJECTS.get("minPropertiesFailure"))
                .expectedPointer("#")
                .expectedKeyword("minProperties")
                .expect();
    }

    @Test
    public void multipleAdditionalProperties() throws Exception {
        ObjectSchema subject = ObjectSchema.builder().additionalProperties(false).build();
        try {
            subject.validate(new JSONObject("{\"a\":true,\"b\":true}"));
            Assert.fail("did not throw exception for multiple additional properties");
        } catch (ValidationException e) {
            Assert.assertEquals("#: 2 schema violations found", e.getMessage());
            Assert.assertEquals(2, e.getCausingExceptions().size());
        }
    }

    @Test
    public void multipleSchemaDepViolation() throws Exception {
        Schema billingAddressSchema = new StringSchema();
        Schema billingNameSchema = StringSchema.builder().minLength(4).build();
        ObjectSchema subject = ObjectSchema.builder()
                .addPropertySchema("name", new StringSchema())
                .addPropertySchema("credit_card", NumberSchema.builder().build())
                .schemaDependency("credit_card", ObjectSchema.builder()
                        .addPropertySchema("billing_address", billingAddressSchema)
                        .addRequiredProperty("billing_address")
                        .addPropertySchema("billing_name", billingNameSchema)
                        .build())
                .schemaDependency("name", ObjectSchema.builder()
                        .addRequiredProperty("age")
                        .build())
                .build();
        try {
            subject.validate(OBJECTS.get("schemaDepViolation"));
            Assert.fail("did not throw ValidationException");
        } catch (ValidationException e) {
            ValidationException creditCardFailure = e.getCausingExceptions().get(0);
            ValidationException ageFailure = e.getCausingExceptions().get(1);
            // due to schemaDeps being stored in (unsorted) HashMap, the exceptions may need to be swapped
            if (creditCardFailure.getCausingExceptions().size() == 0) {
                ValidationException tmp = creditCardFailure;
                creditCardFailure = ageFailure;
                ageFailure = tmp;
            }
            ValidationException billingAddressFailure = creditCardFailure.getCausingExceptions().get(0);
            Assert.assertEquals("#/billing_address", billingAddressFailure.getPointerToViolation());
            Assert.assertEquals(billingAddressSchema, billingAddressFailure.getViolatedSchema());
            ValidationException billingNameFailure = creditCardFailure
                    .getCausingExceptions().get(1);
            Assert.assertEquals("#/billing_name", billingNameFailure.getPointerToViolation());
            Assert.assertEquals(billingNameSchema, billingNameFailure.getViolatedSchema());
            Assert.assertEquals("#", ageFailure.getPointerToViolation());
            Assert.assertEquals("#: required key [age] not found", ageFailure.getMessage());
        }
    }

    @Test
    public void multipleViolations() throws Exception {
        Schema subject = ObjectSchema.builder()
                .addPropertySchema("numberProp", new NumberSchema())
                .patternProperty("^string.*", new StringSchema())
                .addPropertySchema("boolProp", BooleanSchema.INSTANCE)
                .addRequiredProperty("boolProp")
                .build();
        try {
            subject.validate(OBJECTS.get("multipleViolations"));
            Assert.fail("did not throw exception for 3 schema violations");
        } catch (ValidationException e) {
            Assert.assertEquals(3, e.getCausingExceptions().size());
            Assert.assertEquals(1, TestSupport.countCauseByJsonPointer(e, "#"));
            Assert.assertEquals(1, TestSupport.countCauseByJsonPointer(e, "#/numberProp"));
            Assert.assertEquals(1, TestSupport.countCauseByJsonPointer(e, "#/stringPatternMatch"));

            List<String> messages = e.getAllMessages();
            Assert.assertEquals(3, messages.size());
            Assert.assertEquals(1, TestSupport.countMatchingMessage(messages, "#:"));
            Assert.assertEquals(1, TestSupport.countMatchingMessage(messages, "#/numberProp:"));
            Assert.assertEquals(1, TestSupport.countMatchingMessage(messages, "#/stringPatternMatch:"));
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void multipleViolationsNested() throws Exception {
        Callable<ObjectSchema.Builder> newBuilder = new Callable<ObjectSchema.Builder>() {
            @Override
            public ObjectSchema.Builder call() {
                return ObjectSchema.builder()
                        .addPropertySchema("numberProp", new NumberSchema())
                        .patternProperty("^string.*", new StringSchema())
                        .addPropertySchema("boolProp", BooleanSchema.INSTANCE)
                        .addRequiredProperty("boolProp");
            }
        };

        Schema nested2 = newBuilder.call().build();
        Schema nested1 = newBuilder.call().addPropertySchema("nested", nested2).build();
        Schema subject = newBuilder.call().addPropertySchema("nested", nested1).build();

        try {
            subject.validate(OBJECTS.get("multipleViolationsNested"));
            Assert.fail("did not throw exception for 9 schema violations");
        } catch (ValidationException subjectException) {
            Assert.assertEquals("#: 9 schema violations found", subjectException.getMessage());
            Assert.assertEquals(4, subjectException.getCausingExceptions().size());
            Assert.assertEquals(1, TestSupport.countCauseByJsonPointer(subjectException, "#"));
            Assert.assertEquals(1, TestSupport.countCauseByJsonPointer(subjectException, "#/numberProp"));
            Assert.assertEquals(1, TestSupport.countCauseByJsonPointer(subjectException, "#/stringPatternMatch"));
            Assert.assertEquals(1, TestSupport.countCauseByJsonPointer(subjectException, "#/nested"));

            ValidationException nested1Exception = FluentIterable.from(subjectException.getCausingExceptions())
                    .firstMatch(new Predicate<ValidationException>() {
                        @Override
                        public boolean apply(ValidationException ex) {
                            return ex.getPointerToViolation().equals("#/nested");
                        }
                    })
                    .get();
            Assert.assertEquals("#/nested: 6 schema violations found", nested1Exception.getMessage());
            Assert.assertEquals(4, nested1Exception.getCausingExceptions().size());
            Assert.assertEquals(1, TestSupport.countCauseByJsonPointer(nested1Exception, "#/nested"));
            Assert.assertEquals(1, TestSupport.countCauseByJsonPointer(nested1Exception, "#/nested/numberProp"));
            Assert.assertEquals(1,
                    TestSupport.countCauseByJsonPointer(nested1Exception, "#/nested/stringPatternMatch"));
            Assert.assertEquals(1, TestSupport.countCauseByJsonPointer(nested1Exception, "#/nested/nested"));

            ValidationException nested2Exception = FluentIterable.from(nested1Exception.getCausingExceptions())
                    .firstMatch(new Predicate<ValidationException>() {
                        @Override
                        public boolean apply(ValidationException ex) {
                            return ex.getPointerToViolation().equals("#/nested/nested");
                        }
                    })
                    .get();

            Assert.assertEquals("#/nested/nested: 3 schema violations found", nested2Exception.getMessage());
            Assert.assertEquals(3, nested2Exception.getCausingExceptions().size());
            Assert.assertEquals(1, TestSupport.countCauseByJsonPointer(nested2Exception, "#/nested/nested"));
            Assert.assertEquals(1, TestSupport.countCauseByJsonPointer(nested2Exception, "#/nested/nested/numberProp"));
            Assert.assertEquals(1,
                    TestSupport.countCauseByJsonPointer(nested2Exception, "#/nested/nested/stringPatternMatch"));

            List<String> messages = subjectException.getAllMessages();
            Assert.assertEquals(9, messages.size());
            Assert.assertEquals(1, TestSupport.countMatchingMessage(messages, "#:"));
            Assert.assertEquals(1, TestSupport.countMatchingMessage(messages, "#/numberProp:"));
            Assert.assertEquals(1, TestSupport.countMatchingMessage(messages, "#/stringPatternMatch:"));
            Assert.assertEquals(1, TestSupport.countMatchingMessage(messages, "#/nested:"));
            Assert.assertEquals(1, TestSupport.countMatchingMessage(messages, "#/nested/numberProp:"));
            Assert.assertEquals(1, TestSupport.countMatchingMessage(messages, "#/nested/stringPatternMatch:"));
            Assert.assertEquals(1, TestSupport.countMatchingMessage(messages, "#/nested/nested:"));
            Assert.assertEquals(1, TestSupport.countMatchingMessage(messages, "#/nested/nested/numberProp:"));
            Assert.assertEquals(1, TestSupport.countMatchingMessage(messages, "#/nested/nested/stringPatternMatch:"));
        }
    }

    @Test
    public void noAdditionalProperties() throws Exception {
        ObjectSchema subject = ObjectSchema.builder().additionalProperties(false).build();
        TestSupport.expectFailure(subject, "#", OBJECTS.get("propertySchemaViolation"));
    }

    @Test
    public void noProperties() throws Exception {
        ObjectSchema.builder().build().validate(OBJECTS.get("noProperties"));
    }

    @Test
    public void notRequireObject() {
        ObjectSchema.builder().requiresObject(false).build().validate("foo");
    }

    @Test
    public void patternPropertyOnEmptyObjct() {
        ObjectSchema.builder()
                .patternProperty("b_.*", BooleanSchema.INSTANCE)
                .build().validate(new JSONObject());
    }

    @Test
    public void patternPropertyOverridesAdditionalPropSchema() throws Exception {
        ObjectSchema.builder()
                .schemaOfAdditionalProperties(new NumberSchema())
                .patternProperty("aa.*", BooleanSchema.INSTANCE)
                .build().validate(OBJECTS.get("patternPropertyOverridesAdditionalPropSchema"));
    }

    @Test
    public void patternPropertyViolation() throws Exception {
        ObjectSchema subject = ObjectSchema.builder()
                .patternProperty("^b_.*", BooleanSchema.INSTANCE)
                .patternProperty("^s_.*", new StringSchema())
                .build();
        TestSupport.expectFailure(subject, BooleanSchema.INSTANCE, "#/b_1",
                OBJECTS.get("patternPropertyViolation"));
    }

    @Test
    public void patternPropsOverrideAdditionalProps() throws Exception {
        ObjectSchema.builder()
                .patternProperty("^v.*", EmptySchema.INSTANCE)
                .additionalProperties(false)
                .build().validate(OBJECTS.get("patternPropsOverrideAdditionalProps"));
    }

    @Test
    public void propertyDepViolation() throws Exception {
        ObjectSchema subject = ObjectSchema.builder()
                .addPropertySchema("ifPresent", NullSchema.INSTANCE)
                .addPropertySchema("mustBePresent", BooleanSchema.INSTANCE)
                .propertyDependency("ifPresent", "mustBePresent")
                .build();
        TestSupport.failureOf(subject)
                .input(OBJECTS.get("propertyDepViolation"))
                .expectedKeyword("dependencies")
                .expect();
    }

    @Test
    public void propertySchemaViolation() throws Exception {
        ObjectSchema subject = ObjectSchema.builder()
                .addPropertySchema("boolProp", BooleanSchema.INSTANCE).build();
        TestSupport.expectFailure(subject, BooleanSchema.INSTANCE, "#/boolProp",
                OBJECTS.get("propertySchemaViolation"));
    }

    @Test
    public void requiredProperties() throws Exception {
        ObjectSchema subject = ObjectSchema.builder()
                .addPropertySchema("boolProp", BooleanSchema.INSTANCE)
                .addPropertySchema("nullProp", NullSchema.INSTANCE)
                .addRequiredProperty("boolProp")
                .build();
        TestSupport.failureOf(subject)
                .expectedPointer("#")
                .expectedKeyword("required")
                .input(OBJECTS.get("requiredProperties"))
                .expect();
    }

    @Test
    public void requireObject() {
        TestSupport.expectFailure(ObjectSchema.builder().build(), "#", "foo");
    }

    @Test
    public void schemaDepViolation() throws Exception {
        Schema billingAddressSchema = new StringSchema();
        ObjectSchema subject = ObjectSchema.builder()
                .addPropertySchema("name", new StringSchema())
                .addPropertySchema("credit_card", NumberSchema.builder().build())
                .schemaDependency("credit_card", ObjectSchema.builder()
                        .addPropertySchema("billing_address", billingAddressSchema)
                        .addRequiredProperty("billing_address")
                        .build())
                .build();
        TestSupport.expectFailure(subject, billingAddressSchema, "#/billing_address",
                OBJECTS.get("schemaDepViolation"));
    }

    @Test(expected = SchemaException.class)
    public void schemaForNoAdditionalProperties() {
        ObjectSchema.builder().additionalProperties(false)
                .schemaOfAdditionalProperties(BooleanSchema.INSTANCE).build();
    }

    @Test
    public void testImmutability() {
        ObjectSchema.Builder builder = ObjectSchema.builder();
        builder.propertyDependency("a", "b");
        builder.schemaDependency("a", BooleanSchema.INSTANCE);
        builder.patternProperty("aaa", BooleanSchema.INSTANCE);
        ObjectSchema schema = builder.build();
        builder.propertyDependency("c", "a");
        builder.schemaDependency("b", BooleanSchema.INSTANCE);
        builder.patternProperty("bbb", BooleanSchema.INSTANCE);
        Assert.assertEquals(1, schema.getPropertyDependencies().size());
        Assert.assertEquals(1, schema.getSchemaDependencies().size());
        Assert.assertEquals(1, schema.getPatternProperties().size());
    }

    @Test
    public void typeFailure() {
        TestSupport.failureOf(ObjectSchema.builder().build())
                .expectedKeyword("type")
                .input("a")
                .expect();
    }

    @Test
    @Ignore("TODO: Reflexivity: object does not equal an identical copy of itself")
    public void equalsVerifier() {
        EqualsVerifier.forClass(ObjectSchema.class)
                .withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void toStringTest() throws Exception {
        JSONObject rawSchemaJson = loader.readObj("tostring/objectschema.json");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson, new JSONObject(actual)));
    }

    @Test
    public void toStringNoExplicitType() throws Exception {
        JSONObject rawSchemaJson = loader.readObj("tostring/objectschema.json");
        rawSchemaJson.remove("type");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson, new JSONObject(actual)));
    }

    @Test
    public void toStringNoAdditionalProperties() throws Exception {
        JSONObject rawSchemaJson = loader.readObj("tostring/objectschema.json");
        rawSchemaJson.put("additionalProperties", false);
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson, new JSONObject(actual)));
    }

    @Test
    public void toStringSchemaDependencies() throws Exception {
        JSONObject rawSchemaJson = loader.readObj("tostring/objectschema-schemadep.json");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson, new JSONObject(actual)));
    }
}
