package org.everit.json.schema.loader;

import org.everit.json.schema.Consumer;
import org.everit.json.schema.JSONObjectUtils;
import org.everit.json.schema.ObjectSchema;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static org.everit.json.schema.JSONObjectUtils.requireNonNull;


/**
 * @author erosb
 */
class ObjectSchemaLoader {

    private final LoadingState ls;

    private final SchemaLoader defaultLoader;

    public ObjectSchemaLoader(LoadingState ls, SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    ObjectSchema.Builder load() throws JSONException {
        final ObjectSchema.Builder builder = ObjectSchema.builder();
        ls.ifPresent("minProperties", Integer.class, new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                builder.minProperties(integer);
            }
        });
        ls.ifPresent("maxProperties", Integer.class, new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                builder.maxProperties(integer);
            }
        });
        if (ls.schemaJson.has("properties")) {
            ls.typeMultiplexer(ls.schemaJson.get("properties"))
                    .ifObject().then(new Consumer<JSONObject>() {
                @Override
                public void accept(JSONObject propertyDefs) {
                    try {
                        populatePropertySchemas(propertyDefs, builder);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).requireAny();
        }
        if (ls.schemaJson.has("additionalProperties")) {
            ls.typeMultiplexer("additionalProperties", ls.schemaJson.get("additionalProperties"))
                    .ifIs(Boolean.class)
                    .then(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) {
                            builder.additionalProperties(aBoolean);
                        }
                    })
                    .ifObject()
                    .then(new Consumer<JSONObject>() {
                        @Override
                        public void accept(JSONObject def) {
                            try {
                                builder.schemaOfAdditionalProperties(defaultLoader.loadChild(def).build());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .requireAny();
        }
        if (ls.schemaJson.has("required")) {
            JSONArray requiredJson = ls.schemaJson.getJSONArray("required");
            for (int i = 0; i < requiredJson.length(); i++) {
                builder.addRequiredProperty(requiredJson.getString(i));
            }
        }
        if (ls.schemaJson.has("patternProperties")) {
            JSONObject patternPropsJson = ls.schemaJson.getJSONObject("patternProperties");
            String[] patterns = JSONObjectUtils.getNames(patternPropsJson);
            if (patterns != null) {
                for (String pattern : patterns) {
                    builder.patternProperty(pattern, defaultLoader.loadChild(patternPropsJson.getJSONObject(pattern))
                            .build());
                }
            }
        }
        ls.ifPresent("dependencies", JSONObject.class, new Consumer<JSONObject>() {
            @Override
            public void accept(JSONObject deps) {
                try {
                    addDependencies(builder, deps);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return builder;
    }

    private void populatePropertySchemas(JSONObject propertyDefs,
            ObjectSchema.Builder builder) throws JSONException {
        String[] names = JSONObjectUtils.getNames(propertyDefs);
        if (names == null || names.length == 0) {
            return;
        }
        for (String key : names) {
            addPropertySchemaDefinition(key, propertyDefs.get(key), builder);
        }
    }

    private void addPropertySchemaDefinition(final String keyOfObj, final Object definition,
            final ObjectSchema.Builder builder) throws JSONException {
        ls.typeMultiplexer(definition)
                .ifObject()
                .then(new Consumer<JSONObject>() {
                    @Override
                    public void accept(JSONObject obj) {
                        try {
                            builder.addPropertySchema(keyOfObj, defaultLoader.loadChild(obj).build());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .requireAny();
    }

    private void addDependencies(final ObjectSchema.Builder builder, final JSONObject deps) throws JSONException {
        for (String name : JSONObjectUtils.getNames(deps)) {
            addDependency(builder, name, deps.get(name));
        }
    }

    private void addDependency(final ObjectSchema.Builder builder, final String ifPresent, final Object deps) throws JSONException {
        ls.typeMultiplexer(deps)
                .ifObject()
                .then(new Consumer<JSONObject>() {
                    @Override
                    public void accept(JSONObject obj) {
                        try {
                            builder.schemaDependency(ifPresent, defaultLoader.loadChild(obj).build());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .ifIs(JSONArray.class)
                .then(new Consumer<JSONArray>() {
                    @Override
                    public void accept(JSONArray propNames) {
                        for (int i = 0; i < propNames.length(); i++) {
                            try {
                                builder.propertyDependency(ifPresent, propNames.getString(i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).requireAny();
    }

}
