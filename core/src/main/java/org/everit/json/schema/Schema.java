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

import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONWriter;
import org.json.JSONException;

import java.io.StringWriter;

/**
 * Superclass of all other schema validator classes of this package.
 */
public abstract class Schema {

    /**
     * Abstract builder class for the builder classes of {@code Schema} subclasses. This builder is
     * used to load the generic properties of all types of schemas like {@code title} or
     * {@code description}.
     *
     * @param <S> the type of the schema being built by the builder subclass.
     */
    public abstract static class Builder<S extends Schema> {

        private String title;

        private String description;

        private String id;

        public Builder<S> title(final String title) {
            this.title = title;
            return this;
        }

        public Builder<S> description(final String description) {
            this.description = description;
            return this;
        }

        public Builder<S> id(final String id) {
            this.id = id;
            return this;
        }

        public abstract S build();

    }

    private final String title;

    private final String description;

    private final String id;

    /**
     * Constructor.
     *
     * @param builder the builder containing the optional title, description and id attributes of the schema
     */
    protected Schema(final Builder<?> builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.id = builder.id;
    }

    /**
     * Performs the schema validation.
     *
     * @param subject the object to be validated
     * @throws ValidationException if the {@code subject} is invalid against this schema.
     */
    public abstract void validate(final Object subject);

    /**
     * Determines if this {@code Schema} instance defines any restrictions for the object property
     * denoted by {@code field}. The {@code field} should be a JSON pointer, denoting the property to
     * be queried.
     * <p>
     * For example the field {@code "#/rectangle/a"} is defined by the following schema:
     * <p>
     * <pre>
     * <code>
     * objectWithSchemaRectangleDep" : {
     *   "type" : "object",
     *   "dependencies" : {
     *       "d" : {
     *           "type" : "object",
     *           "properties" : {
     *               "rectangle" : {"$ref" : "#/definitions/Rectangle" }
     *           }
     *       }
     *   },
     *   "definitions" : {
     *       "size" : {
     *           "type" : "number",
     *           "minimum" : 0
     *       },
     *       "Rectangle" : {
     *           "type" : "object",
     *           "properties" : {
     *               "a" : {"$ref" : "#/definitions/size"},
     *               "b" : {"$ref" : "#/definitions/size"}
     *           }
     *       }
     *    }
     * }
     * </code>
     * </pre>
     * <p>
     * The default implementation of this method always returns false.
     *
     * @param field should be a JSON pointer in its string representation.
     * @return {@code true} if the propertty denoted by {@code field} is defined by this schema
     * instance
     */
    public boolean definesProperty(final String field) {
        return false;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    /**
     * Describes the instance as a JSONObject to {@code writer}.
     * <p>
     * First it adds the {@code "title} , {@code "description"} and {@code "id"} properties then calls
     * {@link #describePropertiesTo(JSONPrinter)}, which will add the subclass-specific properties.
     * <p>
     * It is used by {@link #toString()} to serialize the schema instance into its JSON representation.
     *
     * @param writer it will receive the schema description
     */
    public final void describeTo(final JSONPrinter writer) throws JSONException {
        writer.object();
        writer.ifPresent("title", title);
        writer.ifPresent("description", description);
        writer.ifPresent("id", id);
        describePropertiesTo(writer);
        writer.endObject();
    }

    /**
     * Subclasses are supposed to override this method to describe the subclass-specific attributes.
     * This method is called by {@link #describeTo(JSONPrinter)} after adding the generic properties if
     * they are present ({@code id}, {@code title} and {@code description}). As a side effect,
     * overriding subclasses don't have to open and close the object with {@link JSONWriter#object()}
     * and {@link JSONWriter#endObject()}.
     *
     * @param writer it will receive the schema description
     */
    void describePropertiesTo(final JSONPrinter writer) throws JSONException {

    }

    @Override
    public String toString() {
        StringWriter w = new StringWriter();
        try {
            describeTo(new JSONPrinter(w));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return w.getBuffer().toString();
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    /**
     * Since we add state in subclasses, but want those subclasses to be non final, this allows us to
     * have equals methods that satisfy the equals contract.
     * <p>
     * http://www.artima.com/lejava/articles/equality.html
     */
    protected boolean canEqual(final Object other) {
        return (other instanceof Schema);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Schema)) return false;

        Schema that = (Schema) o;
        return that.canEqual(this)
                && (title != null ? title.equals(that.title) : that.title == null)
                && (description != null ? description.equals(that.description) : that.description == null)
                && (id != null ? id.equals(that.id) : that.id == null);
    }
}
