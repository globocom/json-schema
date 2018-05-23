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
 * {@code Not} schema validator.
 */
public class NotSchema extends Schema {

    /**
     * Builder class for {@link NotSchema}.
     */
    public static class Builder extends Schema.Builder<NotSchema> {

        private Schema mustNotMatch;

        @Override
        public NotSchema build() {
            return new NotSchema(this);
        }

        public Builder mustNotMatch(final Schema mustNotMatch) {
            this.mustNotMatch = mustNotMatch;
            return this;
        }

    }

    private final Schema mustNotMatch;

    public NotSchema(final Builder builder) {
        super(builder);
        this.mustNotMatch = requireNonNull(builder.mustNotMatch, "mustNotMatch cannot be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Schema getMustNotMatch() {
        return mustNotMatch;
    }

    @Override
    public void validate(final Object subject) {
        try {
            mustNotMatch.validate(subject);
        } catch (ValidationException e) {
            e.printStackTrace();
            return;
        }
        throw new ValidationException(this, "subject must not be valid against schema " + mustNotMatch,
                "not");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotSchema)) {
            return false;
        }
        NotSchema that = (NotSchema) o;
        return that.canEqual(this) &&
                mustNotMatch != null && mustNotMatch.equals(that.mustNotMatch) &&
                super.equals(that);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mustNotMatch != null ? mustNotMatch.hashCode() : 0);
        return result;
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof NotSchema;
    }

    @Override
    void describePropertiesTo(JSONPrinter writer) throws JSONException {
        writer.key("not");
        mustNotMatch.describeTo(writer);
    }
}
