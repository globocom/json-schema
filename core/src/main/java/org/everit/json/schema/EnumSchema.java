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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Enum schema validator.
 */
public class EnumSchema extends Schema {

    /**
     * Builder class for {@link EnumSchema}.
     */
    public static class Builder extends Schema.Builder<EnumSchema> {

        private Set<Object> possibleValues = new HashSet<>();

        @Override
        public EnumSchema build() {
            return new EnumSchema(this);
        }

        public Builder possibleValue(final Object possibleValue) {
            possibleValues.add(possibleValue);
            return this;
        }

        public Builder possibleValues(final Set<Object> possibleValues) {
            this.possibleValues = possibleValues;
            return this;
        }
    }

    private final Set<Object> possibleValues;

    public EnumSchema(final Builder builder) {
        super(builder);
        possibleValues = Collections.unmodifiableSet(new HashSet<>(builder.possibleValues));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Set<Object> getPossibleValues() {
        return possibleValues;
    }

    @Override
    public void validate(final Object subject) {
        for (Object val : possibleValues) {
            try {
                if (ObjectComparator.deepEquals(val, subject)) {
                    //found one
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        throw new ValidationException(this, String.format("%s is not a valid enum value", subject), "enum");
    }

    @Override
    void describePropertiesTo(final JSONPrinter writer) throws JSONException {
        writer.key("type");
        writer.value("enum");
        writer.key("enum");
        writer.array();
        for (Object value : possibleValues) {
            writer.value(value);
        }
        writer.endArray();
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof EnumSchema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof EnumSchema)) return false;

        EnumSchema that = (EnumSchema) o;
        return that.canEqual(this)
                && possibleValues != null ? possibleValues.equals(that.possibleValues) : that.possibleValues == null
                && super.equals(that);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (possibleValues != null ? possibleValues.hashCode() : 0);
        return result;
    }

}
