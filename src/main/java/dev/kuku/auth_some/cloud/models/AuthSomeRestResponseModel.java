package dev.kuku.auth_some.cloud.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@Setter
public class AuthSomeRestResponseModel<T> {
    private T data;
    private String message;
}
