package dev.kuku.authsome.cloud.rest_controller.project;

import dev.kuku.authsome.cloud.models.ResponseModel;
import dev.kuku.authsome.cloud.models.project.AddIdentityForUserRequest;
import dev.kuku.authsome.cloud.models.project.CreateProjectUserRequest;
import dev.kuku.authsome.cloud.models.project.IdentityVerifiedRequest;
import dev.kuku.authsome.cloud.models.project.UpdatePasswordBody;
import dev.kuku.authsome.core_service.project.api.ProjectService;
import dev.kuku.authsome.core_service.project.api.dto.ProjectUserIdentityToFetch;
import dev.kuku.authsome.core_service.project.api.dto.ProjectUserToFetch;
import dev.kuku.vfl.api.annotation.RootBlock;
import dev.kuku.vfl.api.annotation.VFLAnnotation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class ProjectUserController extends ProjectController {
    public ProjectUserController(VFLAnnotation log, ProjectService projectService) {
        super(log, projectService);
    }

    @PostMapping("/")
    @RootBlock
    public ResponseModel<ProjectUserToFetch> createUser(CreateProjectUserRequest body) {
        log.info("createUser {}", body);
        var created = projectService.createUserForProject(currentUser(), body.projectId, body.username, body.password);
        return new ResponseModel<>(created, null);
    }

    @PutMapping("/pwd")
    @RootBlock
    public ResponseModel<ProjectUserToFetch> updatePassword(@RequestBody UpdatePasswordBody body) {
        log.info("updatePassword {}", body);
        var created = projectService.updatePassword(currentUser(), body.projectId, body.userId, body.updatedPassword);
        return new ResponseModel<>(created, null);
    }

    /**
     * add identities to project
     *
     * @return created identity
     */
    @PostMapping("/identities")
    @RootBlock
    public ResponseModel<ProjectUserIdentityToFetch> addIdentityForUser(AddIdentityForUserRequest body) {
        log.info("addIdentityForUser {}", body);
        var created = projectService.addIdentitiesForUsers(currentUser(),
                body.projectId,
                body.userId,
                body.identityType,
                body.identity,
                body.verified,
                body.isPrimary);
        return new ResponseModel<>(created, null);
    }

    /**
     * Update verified boolean property of identities of users
     *
     * @return updated entry
     */
    @PostMapping("/identities-verified")
    @RootBlock
    public ResponseModel<ProjectUserIdentityToFetch> setIdentityVerified(IdentityVerifiedRequest body) {
        log.info("setIdentityVerified {}", body);
        var updated = projectService.setIdentityVerified(currentUser(), body.projectId, body.projectUserId, body.identityType, body.identity, body.verified);
        return new ResponseModel<>(updated, null);
    }

    //TODO update password in various way endpoint such as current and updated, otp, etc
    //TODO delete endpoints
    //TODO sign ins.... sign up? sign up is tricky because sometimes we need identity FIRST and then user info, sometimes the othe rway around
    //TODO need setting service for these stuffs
}
