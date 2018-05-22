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
package org.everit.json.schema.loader;

import com.google.common.base.Optional;

import org.everit.json.schema.AbstractFormatValidator;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CustomFormatValidatorTest {

    private final ResourceLoader loader = ResourceLoader.DEFAULT;

    static class EvenCharNumValidator extends AbstractFormatValidator {

        @Override
        public Optional<String> validate(final String subject) {
            if (subject.length() % 2 == 0) {
                return Optional.absent();
            } else {
                return Optional.of(String.format("the length of srtring [%s] is odd", subject));
            }
        }

        @Override
        public String formatName() {
            return "evenlength";
        }
    }

    @Test
    public void test() throws Exception {
        SchemaLoader schemaLoader = SchemaLoader.builder()
                .schemaJson(baseSchemaJson())
                .addFormatValidator("evenlength", new EvenCharNumValidator())
                .build();
        try {
            schemaLoader.load().build().validate(loader.readObj("customformat-data.json"));
            Assert.fail("did not throw exception");
        } catch (ValidationException ve) {
        }
    }

    @Test
    public void nameOverride() throws Exception {
        JSONObject rawSchemaJson = baseSchemaJson();

        JSONObject idPropSchema = rawSchemaJson.getJSONObject("properties").getJSONObject("id");
        idPropSchema.put("format", "somethingelse");

        SchemaLoader schemaLoader = SchemaLoader.builder()
                .schemaJson(rawSchemaJson)
                .addFormatValidator("somethingelse", new EvenCharNumValidator())
                .build();

        Object actual = fetchFormatValueFromOutputJson(schemaLoader);
        assertEquals("somethingelse", actual);
    }

    private Object fetchFormatValueFromOutputJson(SchemaLoader schemaLoader) throws JSONException {
        return new JSONObject(schemaLoader.load().build().toString())
                .getJSONObject("properties").getJSONObject("id").get("format");
    }

    private JSONObject baseSchemaJson() throws Exception {
        return loader.readObj("customformat-schema.json");
    }

    @Test
    public void formatValidatorWithoutExplicitName() throws Exception {
        SchemaLoader schemaLoader = SchemaLoader.builder()
                .schemaJson(baseSchemaJson())
                .addFormatValidator(new EvenCharNumValidator())
                .build();
        Object actual = fetchFormatValueFromOutputJson(schemaLoader);
        assertEquals("evenlength", actual);
    }

}
