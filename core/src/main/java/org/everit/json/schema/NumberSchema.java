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

import java.math.BigDecimal;

/**
 * Number schema validator.
 */
public class NumberSchema extends Schema {

    /**
     * Builder class for {@link NumberSchema}.
     */
    public static class Builder extends Schema.Builder<NumberSchema> {

        private Number minimum;

        private Number maximum;

        private Number multipleOf;

        private boolean exclusiveMinimum = false;

        private boolean exclusiveMaximum = false;

        private boolean requiresNumber = true;

        private boolean requiresInteger = false;

        @Override
        public NumberSchema build() {
            return new NumberSchema(this);
        }

        public Builder exclusiveMaximum(final boolean exclusiveMaximum) {
            this.exclusiveMaximum = exclusiveMaximum;
            return this;
        }

        public Builder exclusiveMinimum(final boolean exclusiveMinimum) {
            this.exclusiveMinimum = exclusiveMinimum;
            return this;
        }

        public Builder maximum(final Number maximum) {
            this.maximum = maximum;
            return this;
        }

        public Builder minimum(final Number minimum) {
            this.minimum = minimum;
            return this;
        }

        public Builder multipleOf(final Number multipleOf) {
            this.multipleOf = multipleOf;
            return this;
        }

        public Builder requiresInteger(final boolean requiresInteger) {
            this.requiresInteger = requiresInteger;
            return this;
        }

        public Builder requiresNumber(final boolean requiresNumber) {
            this.requiresNumber = requiresNumber;
            return this;
        }

    }

    private final boolean requiresNumber;
    private final Number minimum;
    private final Number maximum;
    private final Number multipleOf;
    private final boolean exclusiveMinimum;
    private final boolean exclusiveMaximum;
    private final boolean requiresInteger;

    public NumberSchema() {
        this(builder());
    }

    /**
     * Constructor.
     *
     * @param builder the builder object containing validation criteria
     */
    public NumberSchema(final Builder builder) {
        super(builder);
        this.minimum = builder.minimum;
        this.maximum = builder.maximum;
        this.exclusiveMinimum = builder.exclusiveMinimum;
        this.exclusiveMaximum = builder.exclusiveMaximum;
        this.multipleOf = builder.multipleOf;
        this.requiresNumber = builder.requiresNumber;
        this.requiresInteger = builder.requiresInteger;
    }

    public static Builder builder() {
        return new Builder();
    }

    private void checkMaximum(final double subject) {
        if (maximum != null) {
            if (exclusiveMaximum && maximum.doubleValue() <= subject) {
                throw new ValidationException(this, subject + " is not lower than " + maximum,
                        "exclusiveMaximum");
            } else if (maximum.doubleValue() < subject) {
                throw new ValidationException(this, subject + " is not lower or equal to " + maximum,
                        "maximum");
            }
        }
    }

    private void checkMinimum(final double subject) {
        if (minimum != null) {
            if (exclusiveMinimum && subject <= minimum.doubleValue()) {
                throw new ValidationException(this, subject + " is not higher than " + minimum,
                        "exclusiveMinimum");
            } else if (subject < minimum.doubleValue()) {
                throw new ValidationException(this, subject + " is not higher or equal to " + minimum,
                        "minimum");
            }
        }
    }

    private void checkMultipleOf(final double subject) {
        if (multipleOf != null) {
            BigDecimal remainder = BigDecimal.valueOf(subject).remainder(
                    BigDecimal.valueOf(multipleOf.doubleValue()));
            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                throw new ValidationException(this, subject + " is not a multiple of " + multipleOf,
                        "multipleOf");
            }
        }
    }

    public Number getMaximum() {
        return maximum;
    }

    public Number getMinimum() {
        return minimum;
    }

    public Number getMultipleOf() {
        return multipleOf;
    }

    public boolean isExclusiveMaximum() {
        return exclusiveMaximum;
    }

    public boolean isExclusiveMinimum() {
        return exclusiveMinimum;
    }

    public boolean requiresInteger() {
        return requiresInteger;
    }

    @Override
    public void validate(final Object subject) {
        if (!(subject instanceof Number)) {
            if (requiresNumber) {
                throw new ValidationException(this, Number.class, subject);
            }
        } else {
            if (!(subject instanceof Integer || subject instanceof Long) && requiresInteger) {
                throw new ValidationException(this, Integer.class, subject, "type");
            }
            double intSubject = ((Number) subject).doubleValue();
            checkMinimum(intSubject);
            checkMaximum(intSubject);
            checkMultipleOf(intSubject);
        }
    }

    @Override
    void describePropertiesTo(JSONPrinter writer) throws JSONException {
        if (requiresInteger) {
            writer.key("type").value("integer");
        } else if (requiresNumber) {
            writer.key("type").value("number");
        }
        writer.ifPresent("minimum", minimum);
        writer.ifPresent("maximum", maximum);
        writer.ifPresent("multipleOf", multipleOf);
        writer.ifTrue("exclusiveMinimum", exclusiveMinimum);
        writer.ifTrue("exclusiveMaximum", exclusiveMaximum);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof NumberSchema)) return false;

        NumberSchema that = (NumberSchema) o;
        return that.canEqual(this)
                && requiresNumber == that.requiresNumber
                && exclusiveMinimum == that.exclusiveMinimum
                && exclusiveMaximum == that.exclusiveMaximum
                && requiresInteger == that.requiresInteger
                && (minimum != null ? minimum.equals(that.minimum) : that.minimum == null)
                && (maximum != null ? maximum.equals(that.maximum) : that.maximum == null)
                && (multipleOf != null ? multipleOf.equals(that.multipleOf) : that.multipleOf == null)
                && super.equals(that);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (requiresNumber ? 1 : 0);
        result = 31 * result + (minimum != null ? minimum.hashCode() : 0);
        result = 31 * result + (maximum != null ? maximum.hashCode() : 0);
        result = 31 * result + (multipleOf != null ? multipleOf.hashCode() : 0);
        result = 31 * result + (exclusiveMinimum ? 1 : 0);
        result = 31 * result + (exclusiveMaximum ? 1 : 0);
        result = 31 * result + (requiresInteger ? 1 : 0);
        return result;
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof NumberSchema;
    }
}
