package dev.kuku.authsome.cloud.rest_controller;

import dev.kuku.authsome.cloud.models.ResponseModel;
import dev.kuku.authsome.cloud.models.project.CreateProjectRequest;
import dev.kuku.authsome.core_service.project.api.ProjectService;
import dev.kuku.authsome.core_service.project.api.dto.ProjectToFetch;
import dev.kuku.vfl.api.annotation.RootBlock;
import dev.kuku.vfl.api.annotation.SubBlock;
import dev.kuku.vfl.api.annotation.VFLAnnotation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/project")
@RequiredArgsConstructor
public class ProjectController {
    final VFLAnnotation log;
    final ProjectService projectService;

    /**
     * Create project for authsome user
     *
     * @param body payload
     */
    @PostMapping("/")
    @RootBlock
    public ResponseModel<ProjectToFetch> createProject(@RequestBody CreateProjectRequest body) {
        log.info("Create Project {}", body);
        var created = projectService.createProject(currentUser(), body.projectName, body.description);
        return new ResponseModel<>(created, null);
    }

    @PutMapping("/{projectId}")
    @RootBlock
    public ResponseModel<ProjectToFetch> updateProject(@PathVariable String projectId, @RequestParam String description, @RequestParam String name) {
        log.info("Update Project {}, {}, {}", projectId, name, description);
        var updated = projectService.updateProject(currentUser(), projectId, name, description);
        return new ResponseModel<>(updated, null);
    }

    @SubBlock
    String currentUser() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
