package org.jusecase.builders.generator.oddities;

import java.io.IOException;

@SuppressWarnings("unused")
public class EntityWithCheckedExceptionSetters {
    public void setThrowingChecked(String value) throws IOException {
    }

    public void setThrowingException(String value) throws Exception {
    }

    public void setThrowingUnchecked(String value) throws IllegalStateException {
    }

    public void setPlain(String value) {
    }
}
