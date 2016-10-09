package org.jusecase.builders.generator;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.jusecase.Builders.an;
import static org.jusecase.Builders.inputStream;

public class Template {
    private final String content;

    public static Template fromResource(String resourceName) throws IOException {
        try (InputStream is = an(inputStream().withResource(resourceName))) {
            return new Template(IOUtils.toString(is));
        }
    }

    private Template(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
