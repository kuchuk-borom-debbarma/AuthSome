package dev.kuku.authsome.cloud.models.project;

import lombok.ToString;

@ToString
public class UpdatePasswordBody {
    public String projectId;
    public String userId;
    public String updatedPassword;
}
