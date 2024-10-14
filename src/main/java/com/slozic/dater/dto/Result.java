package com.slozic.dater.dto;

import lombok.Getter;

@Getter
public class Result<T, P> {
    T payload;
    P parameters;
    String error;

    public Result(T payload) {
        this.payload = payload;
    }

    public Result(T payload, P parameters) {
        this.payload = payload;
        this.parameters = parameters;
    }

    public Result(T payload, P parameters, String error) {
        this.payload = payload;
        this.parameters = parameters;
        this.error = error;
    }

    public boolean isSuccess(){
        return payload != null;
    }
}
