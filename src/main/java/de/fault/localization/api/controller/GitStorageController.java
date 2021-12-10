package de.fault.localization.api.controller;

import de.fault.localization.api.model.GitRepository;
import de.fault.localization.api.services.GitStorageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controller for managing repositories
 */
@Log
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path = "/api/repository")
@Api(value = "repository", tags = "repository", description = "Operations pertaining to repositories")
public class GitStorageController {

    private final GitStorageService service;

    public GitStorageController(final GitStorageService service) {
        this.service = service;
    }

    @ApiOperation(value = "Returns lists with all repositories")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully retrieved list")})
    @GetMapping(path = "/list")
    public List<GitRepository> list() {
        log.info("list gitStorages");
        return this.service.listAll();
    }

}
