package org.jusecase.builders.generator;

import java.util.HashMap;
import java.util.Map;

public class TemplateProcessor {
    private final Template template;
    private final Map<String, String> properties;

    public TemplateProcessor(Template template) {
        this.template = template;
        this.properties = new HashMap<>();
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public String process() {
        String content = template.getContent();
        for (Map.Entry<String, String> property : properties.entrySet()) {
           content = content.replace("${" + property.getKey() + "}", property.getValue());
        }
        return content;
    }

    public void setProperties(TemplateProcessor processor) {
        properties.putAll(processor.properties);
    }
}
