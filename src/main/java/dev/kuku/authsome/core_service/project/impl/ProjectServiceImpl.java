package dev.kuku.authsome.core_service.project.impl;

import dev.kuku.authsome.core_service.project.api.ProjectService;
import dev.kuku.vfl.api.annotation.VFLAnnotation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final VFLAnnotation log;

}
