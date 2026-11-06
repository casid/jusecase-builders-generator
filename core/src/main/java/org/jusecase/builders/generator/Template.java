package org.jusecase.builders.generator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Template {
    private final String content;

    public static Template fromResource(String resourceName) throws IOException {
        try (InputStream is = Template.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new FileNotFoundException("Template resource not found: " + resourceName);
            }
            return new Template(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private Template(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
