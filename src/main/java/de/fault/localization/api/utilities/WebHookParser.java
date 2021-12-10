package de.fault.localization.api.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.fault.localization.api.model.ParsedWebHook;
import de.fault.localization.api.model.constants.VCS;
import lombok.val;

/**
 * contains methods for parsing web hooks
 */
public class WebHookParser {

	private WebHookParser(){

	}

	private static final Gson gson = new Gson();

	/**
	 * @return parsed webhook
	 */
	public static ParsedWebHook parseFrom(final String json) {

		val webHook = gson.fromJson(json, JsonObject.class);

		// see https://docs.gitlab.com/ee/user/project/integrations/webhooks.html
		val vcs = webHook.has("object_kind") ? VCS.GITLAB : VCS.GITHUB;

		final String absoluteUrl;
		val branch = webHook.get("ref").getAsString().split("/")[2].trim();

		val commitIdNew = webHook.get("after").getAsString();
		val commitIdOld = webHook.get("before").getAsString();

		if (vcs.equals(VCS.GITLAB)) {
			absoluteUrl = webHook.getAsJsonObject("project").get("web_url").getAsString();
		} else {
			absoluteUrl = webHook.getAsJsonObject("repository").get("html_url").getAsString();
		}

		return new ParsedWebHook(absoluteUrl, branch, vcs, commitIdNew, commitIdOld);
	}
}
