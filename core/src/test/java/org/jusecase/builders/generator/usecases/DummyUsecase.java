package org.jusecase.builders.generator.usecases;

import org.jusecase.Usecase;

@SuppressWarnings("unchecked")
public class DummyUsecase implements Usecase<DummyUsecase.Request, DummyUsecase.Response> {

    @Override
    public Response execute(Request request) {
        return null;
    }

    @SuppressWarnings("unused")
    public static class Request {
        public String dummyEmail;
        public String dummyPassword;
    }

    @SuppressWarnings("unused")
    public static class Response {
        public String dummyMessage;
    }

    @SuppressWarnings("unused")
    public class NonStaticNestedClassWillBeIgnored {
        public String dummyField;
    }
}
