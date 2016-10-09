package org.jusecase.builders.generator.usecases;

import org.jusecase.Usecase;

@SuppressWarnings("unchecked")
public class DummyUsecase implements Usecase<DummyUsecase.Request, DummyUsecase.Response> {

    @Override
    public Response execute(Request request) {
        return null;
    }

    public static class Request {
        public String dummyEmail;
        public String dummyPassword;
    }

    public static class Response {

    }
}
