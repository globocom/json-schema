package org.everit.json.schema;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class ResourceLoader {

    public static final ResourceLoader DEFAULT = new ResourceLoader("/org/everit/jsonvalidator/");

    private final String rootPath;

    public ResourceLoader(String rootPath) {
        this.rootPath = requireNonNull(rootPath, "rootPath cannot be null");
    }

    public JSONObject readObj(String relPath) throws JSONException {
        InputStream stream = getStream(relPath);
        String objContent = new BufferedReader(new InputStreamReader(stream))
                .lines().collect(Collectors.joining("\n"));
        return new JSONObject(new JSONTokener(objContent));
    }

    public InputStream getStream(String relPath) {
        String absPath = rootPath + relPath;
        InputStream rval = getClass().getResourceAsStream(absPath);
        if (rval == null) {
            throw new IllegalArgumentException(
                    format("failed to load resource by relPath [%s].\n"
                            + "InputStream by path [%s] is null", relPath, absPath));
        }
        return rval;
    }

}
