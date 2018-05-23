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
import org.json.JSONException;

import static org.everit.json.schema.JSONObjectUtils.requireNonNull;

/**
 * This class is used by {@link org.everit.json.schema.loader.SchemaLoader} to resolve JSON pointers
 * during the construction of the schema. This class has been made mutable to permit the loading of
 * recursive schemas.
 */
public class ReferenceSchema extends Schema {

    /**
     * Builder class for {@link ReferenceSchema}.
     */
    public static class Builder extends Schema.Builder<ReferenceSchema> {

        private ReferenceSchema retval;

        /**
         * The value of {@code "$ref"}
         */
        private String refValue = "";

        /**
         * This method caches its result, so multiple invocations will return referentially the same
         * {@link ReferenceSchema} instance.
         */
        @Override
        public ReferenceSchema build() {
            if (retval == null) {
                retval = new ReferenceSchema(this);
            }
            return retval;
        }

        public Builder refValue(String refValue) {
            this.refValue = refValue;
            return this;
        }
    }

    private final String refValue;
    private Schema referredSchema;

    public ReferenceSchema(final Builder builder) {
        super(builder);
        this.refValue = requireNonNull(builder.refValue, "refValue cannot be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void validate(final Object subject) {
        if (referredSchema == null) {
            throw new IllegalStateException("referredSchema must be injected before validation");
        }
        referredSchema.validate(subject);
    }

    @Override
    public boolean definesProperty(String field) {
        if (referredSchema == null) {
            throw new IllegalStateException("referredSchema must be injected before validation");
        }
        return referredSchema.definesProperty(field);
    }

    public Schema getReferredSchema() {
        return referredSchema;
    }

    /**
     * Called by {@link org.everit.json.schema.loader.SchemaLoader#load()} to set the referred root
     * schema after completing the loading process of the entire schema document.
     *
     * @param referredSchema the referred schema
     */
    public void setReferredSchema(final Schema referredSchema) {
        if (this.referredSchema != null) {
            throw new IllegalStateException("referredSchema can be injected only once");
        }
        this.referredSchema = referredSchema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ReferenceSchema)) return false;

        ReferenceSchema that = (ReferenceSchema) o;
        return that.canEqual(this)
                && (refValue != null ? refValue.equals(that.refValue) : that.refValue == null)
                && (referredSchema != null ? referredSchema.equals(that.referredSchema) : that.referredSchema == null)
                && super.equals(that);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (refValue != null ? refValue.hashCode() : 0);
        result = 31 * result + (referredSchema != null ? referredSchema.hashCode() : 0);
        return result;
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof ReferenceSchema;
    }

    @Override
    void describePropertiesTo(JSONPrinter writer) throws JSONException {
        writer.key("$ref");
        writer.value(refValue);
    }
}
