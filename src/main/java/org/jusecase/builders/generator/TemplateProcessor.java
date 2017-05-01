package org.jusecase.builders.generator;

import java.util.HashMap;
import java.util.Map;

public class TemplateProcessor {
    private final Template template;
    private final String lineSeparator;
    private final Map<String, String> properties;

    public TemplateProcessor(Template template, String lineSeparator) {
        this.template = template;
        this.lineSeparator = lineSeparator;
        this.properties = new HashMap<>();
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public String process() {
        String content = template.getContent();
        if (!"\n".equals(lineSeparator)) {
            content = content.replace("\n", lineSeparator);
        }

        for (Map.Entry<String, String> property : properties.entrySet()) {
           content = content.replace("${" + property.getKey() + "}", property.getValue());
        }
        return content;
    }

    public void setProperties(TemplateProcessor processor) {
        properties.putAll(processor.properties);
    }
}
