package de.fault.localization.api.controller;

import de.fault.localization.api.services.hook.GitWebHookParseService;
import de.fault.localization.api.services.monitor.MonitorDaemon;
import de.fault.localization.api.services.monitor.RepositoryMonitor;
import de.fault.localization.api.utilities.RequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Level;

/**
 * This controller handles incoming web hooks from both github and gitlab and triggers the comparison process
 */
@Log
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path = "/api/hook")
@Api(value = "hook", tags = "hook", description = "Webhook listener")
public class WebHookController {

    private final GitWebHookParseService gitWebHookParseService;
    private final MonitorDaemon daemon;

    @Autowired
    public WebHookController(final GitWebHookParseService accountManagementService) {
        this.gitWebHookParseService = accountManagementService;
        this.daemon = new MonitorDaemon(this.gitWebHookParseService.getGitWebHookHandler());
    }

    @ApiOperation(value = "Endpoint being triggered by pushes on gitlab repository")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully triggered actions")})
    @PostMapping(path = "/push")
    public ResponseEntity<String> handleRequest(final HttpServletRequest request) {
        try {
            log.info("handle request from " + RequestUtil.getIP(request));
            this.gitWebHookParseService.handle(request);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (final Exception e) {
            log.log(Level.SEVERE, "there was an error when processing request", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ApiOperation(value = "Endpoint that can be triggered manually")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully triggered actions")})
    @PostMapping(path = "/trigger")
    public ResponseEntity<String> trigger(final HttpServletRequest request, final RepositoryMonitor monitor) {
        try {
            log.info("handle request from " + RequestUtil.getIP(request));
            this.daemon.startComparison(monitor);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (final Exception e) {
            log.log(Level.SEVERE, "there was an error when processing request", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
