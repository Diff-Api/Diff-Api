package de.fault.localization.api.services.hook;

import de.fault.localization.api.exceptions.WebHookHandlerException;
import de.fault.localization.api.services.GitStorageService;
import de.fault.localization.api.utilities.WebHookParser;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * this class parses all incoming web hooks
 */
@Service
public class GitWebHookParseService {

	@Getter
	private final GitWebHookHandler gitWebHookHandler;

	@Autowired
	public GitWebHookParseService(final GitStorageService gitStorageService) {
		this.gitWebHookHandler = new GitWebHookHandler(gitStorageService);
	}

	/**
	 * @param request - the incoming http request containing the webhook as body
	 */
	public void handle(final HttpServletRequest request) throws WebHookHandlerException, IOException {
		final String plainBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		val parseResult = WebHookParser.parseFrom(plainBody);
		this.gitWebHookHandler.handleRequest(parseResult);
	}
}
