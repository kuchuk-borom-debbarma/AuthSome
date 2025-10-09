package dev.kuku.authsome.cloud.models.project;

import lombok.ToString;

import java.util.Optional;

@ToString
public class CreateProjectUserRequest {
    public String projectId;
    public String username;
    public Optional<String> password;
}
