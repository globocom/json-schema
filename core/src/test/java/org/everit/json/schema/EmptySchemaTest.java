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

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class EmptySchemaTest {

    @Test
    public void testValidate() {
        EmptySchema.INSTANCE.validate("something");
    }

    @Test
    public void testBuilder() {
        Assert.assertEquals(EmptySchema.builder().build(), EmptySchema.builder().build());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("{}", EmptySchema.INSTANCE.toString());
    }

    private JSONObject json(final String title, final String description, final String id) throws JSONException {
        return new JSONObject(EmptySchema.builder().title(title).description(description).id(id)
                .build().toString());
    }

    @Test
    public void testOnlySchemaDescription() throws Exception {
        JSONObject actual = json(null, "descr", null);
        Assert.assertEquals(1, JSONObjectUtils.getNames(actual).length);
        Assert.assertEquals("descr", actual.get("description"));
    }

    @Test
    public void testOnlyTitle() throws Exception {
        JSONObject actual = json("my title", null, null);
        Assert.assertEquals(1, JSONObjectUtils.getNames(actual).length);
        Assert.assertEquals("my title", actual.get("title"));
    }

    @Test
    public void testOnlyId() throws Exception {
        JSONObject actual = json(null, null, "my/id");
        Assert.assertEquals(1, JSONObjectUtils.getNames(actual).length);
        Assert.assertEquals("my/id", actual.get("id"));
    }

    @Test
    public void testAllGenericProps() throws Exception {
        JSONObject actual = json("my title", "my description", "my/id");
        Assert.assertEquals(3, JSONObjectUtils.getNames(actual).length);
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(EmptySchema.class)
                .withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }
}
