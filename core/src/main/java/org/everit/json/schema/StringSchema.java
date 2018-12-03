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

import com.google.common.base.Function;

import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.everit.json.schema.JSONObjectUtils.requireNonNull;


/**
 * {@code String} schema validator.
 */
public class StringSchema extends Schema {

    /**
     * Builder class for {@link StringSchema}.
     */
    public static class Builder extends Schema.Builder<StringSchema> {

        private Integer minLength;

        private Integer maxLength;

        private String pattern;

        private boolean requiresString = true;

        private FormatValidator formatValidator = FormatValidator.NONE;

        @Override
        public StringSchema build() {
            return new StringSchema(this);
        }

        /**
         * Setter for the format validator. It should be used in conjunction with
         * {@link AbstractFormatValidator#forFormat(String)} if a {@code "format"} value is found in a schema
         * json.
         *
         * @param formatValidator the format validator
         * @return {@code this}
         */
        public Builder formatValidator(final FormatValidator formatValidator) {
            this.formatValidator = requireNonNull(formatValidator, "formatValidator cannot be null");
            return this;
        }

        public Builder maxLength(final Integer maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder minLength(final Integer minLength) {
            this.minLength = minLength;
            return this;
        }

        public Builder pattern(final String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder requiresString(final boolean requiresString) {
            this.requiresString = requiresString;
            return this;
        }

    }

    private final Integer minLength;
    private final Integer maxLength;
    private final Pattern pattern;
    private final boolean requiresString;
    private final FormatValidator formatValidator;

    public StringSchema() {
        this(builder());
    }

    /**
     * Constructor.
     *
     * @param builder the builder object containing validation criteria
     */
    public StringSchema(final Builder builder) {
        super(builder);
        this.minLength = builder.minLength;
        this.maxLength = builder.maxLength;
        this.requiresString = builder.requiresString;
        if (builder.pattern != null) {
            this.pattern = Pattern.compile(builder.pattern);
        } else {
            this.pattern = null;
        }
        this.formatValidator = builder.formatValidator;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public Pattern getPattern() {
        return pattern;
    }

    private List<ValidationException> testLength(final String subject) {
        int actualLength = subject.codePointCount(0, subject.length());
        List<ValidationException> rval = new ArrayList<>();
        if (minLength != null && actualLength < minLength.intValue()) {
            rval.add(new ValidationException(this, "expected minLength: " + minLength + ", actual: "
                    + actualLength, "minLength"));
        }
        if (maxLength != null && actualLength > maxLength.intValue()) {
            rval.add(new ValidationException(this, "expected maxLength: " + maxLength + ", actual: "
                    + actualLength, "maxLength"));
        }
        return rval;
    }

    private List<ValidationException> testPattern(final String subject) {
        if (pattern != null && !pattern.matcher(subject).find()) {
            return Arrays.asList(new ValidationException(this, String.format(
                    "string [%s] does not match pattern %s",
                    subject, pattern.pattern()), "pattern"));
        }
        return Collections.emptyList();
    }

    @Override
    public void validate(final Object subject) {
        if (!(subject instanceof String)) {
            if (requiresString) {
                throw new ValidationException(this, String.class, subject);
            }
        } else {
            String stringSubject = (String) subject;
            List<ValidationException> rval = new ArrayList<>();
            rval.addAll(testLength(stringSubject));
            rval.addAll(testPattern(stringSubject));
            rval.addAll(formatValidator.validate(stringSubject)
                    .transform(new Function<String, ValidationException>() {
                        @Override
                        public ValidationException apply(String failure) {
                            return new ValidationException(StringSchema.this, failure, "format");
                        }
                    })
                    .asSet());
            ValidationException.throwFor(this, rval);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof StringSchema)) return false;

        StringSchema that = (StringSchema) o;
        return that.canEqual(this)
                && requiresString == that.requiresString
                && (minLength != null ? minLength.equals(that.minLength) : that.minLength == null)
                && (maxLength != null ? maxLength.equals(that.maxLength) : that.maxLength == null)
                && (pattern != null ? pattern.equals(that.pattern) : that.pattern == null)
                && (formatValidator != null ? formatValidator.equals(that.formatValidator) : that.formatValidator == null)
                && super.equals(that);
    }

    private String patternIfNotNull(Pattern pattern) {
        if (pattern == null) {
            return null;
        } else {
            return pattern.pattern();
        }
    }

    public FormatValidator getFormatValidator() {
        return formatValidator;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (minLength != null ? minLength.hashCode() : 0);
        result = 31 * result + (maxLength != null ? maxLength.hashCode() : 0);
        result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
        result = 31 * result + (requiresString ? 1 : 0);
        result = 31 * result + (formatValidator != null ? formatValidator.hashCode() : 0);
        return result;
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof StringSchema;
    }

    @Override
    void describePropertiesTo(JSONPrinter writer) throws JSONException {
        if (requiresString) {
            writer.key("type").value("string");
        }
        writer.ifPresent("minLength", minLength);
        writer.ifPresent("maxLength", maxLength);
        writer.ifPresent("pattern", pattern);
        if (formatValidator != null) {
            writer.key("format").value(formatValidator.formatName());
        }
    }
}
