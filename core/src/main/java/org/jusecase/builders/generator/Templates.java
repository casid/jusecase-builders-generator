package org.jusecase.builders.generator;

import java.io.IOException;

public class Templates {
    public static Template BUILDER_CLASS = templateResource("builder-class");
    public static Template BUILDER_CONSTRUCTION = templateResource("builder-construction");
    public static Template BUILDER_METHOD_SETTER = templateResource("builder-method-setter");
    public static Template BUILDER_METHOD_DIRECT = templateResource("builder-method-direct");

    private static Template templateResource(String name) {
        try {
            return Template.fromResource("templates/" + name + ".txt");
        } catch (IOException e) {
            return null;
        }
    }
}
