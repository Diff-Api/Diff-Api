package de.fault.localization.api.external;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.fault.localization.api.services.settings.Config;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * this class is responsible for creating the issues in the ccims backend
 *
 * @see <a href="https://github.com/ccims/ccims-backend-gql">https://github.com/ccims/ccims-backend-gql</a>
 */
public class CCIMSApi {

    // templates for graphql queries
    private static final String FILTER_URL_TEMPLATE = "{ components(filterBy: {repositoryURL: \"%s\"}) { nodes { id } } }";
    private static final String CREATE_COMPONENT_TEMPLATE = "mutation CreateComponent { createComponent(input: { repositoryURL: \"%s\" name: \"%s\" }) { clientMutationID component { id name } } }";
    private static final String CREATE_ISSUE_TEMPLATE = "mutation CreateIssue { createIssue(input: {title: \"%s\", body: \"%s\", components: [\"%s\"], category: BUG}) { issue { id } } }";

    private final static HttpClient httpClient = HttpClients.createDefault();

    private static final Gson gson = new Gson();
    
    private static final String CCIMS_API = Config.get().getCcimsApi();

    private CCIMSApi(){

    }

    /**
     * @param repositoryUrl - repository url
     * @return id of created component
     * @throws IOException        - error contacting server
     * @throws URISyntaxException - api url loaded from config is not a valid uri
     */
    public static String createComponent(final String repositoryUrl, final String name) throws IOException, URISyntaxException {
        val map = sendRequest(String.format(CREATE_COMPONENT_TEMPLATE, repositoryUrl, name));
        val res = map.getAsJsonObject("data").getAsJsonObject("createComponent").getAsJsonObject("component");
        return res.get("id").getAsString();
    }

    /**
     * creates new issue
     *
     * @param title       - title of issue
     * @param body        - markdown generated ticket
     * @param componentId - id of repository component
     * @return id of created issue
     * @throws IOException        - error contacting server
     * @throws URISyntaxException - api url loaded from config is not a valid uri
     */
    public static String createIssue(final String title, final String body, final String componentId) throws IOException, URISyntaxException {
        val map = sendRequest(String.format(CREATE_ISSUE_TEMPLATE, title, body, componentId));
        val res = map.getAsJsonObject("data").getAsJsonObject("createIssue").getAsJsonObject("issue");
        return res.get("id").getAsString();
    }

    /**
     * get component id for repository
     *
     * @param repositoryUrl - url of repository
     * @return component id of matching url, null if no entry found
     * @throws IOException        - error contacting server
     * @throws URISyntaxException - api url loaded from config is not a valid uri
     */
    public static String getComponentId(final String repositoryUrl) throws IOException, URISyntaxException {
        val map = sendRequest(String.format(FILTER_URL_TEMPLATE, repositoryUrl));
        val res = map.getAsJsonObject("data").getAsJsonObject("components").getAsJsonArray("nodes");
        if (res.size() > 0) {
            return res.get(0).getAsJsonObject().get("id").getAsString();
        }
        return null;
    }

    /**
     * sends final query to api and returns results
     *
     * @param query - graphql query to send
     * @return results parsed as json object
     * @throws IOException        - error contacting server
     * @throws URISyntaxException - api url loaded from config is not a valid uri
     */
    private static JsonObject sendRequest(final String query) throws URISyntaxException, IOException {

        final Map<String, String> map = new HashMap<>();
        map.put("query", query);
        map.put("variables", "null");

        final HttpPost httppost = new HttpPost();
        httppost.setEntity(new ByteArrayEntity(gson.toJson(map).getBytes(StandardCharsets.UTF_8)));
        httppost.setHeader("Content-Type", "application/json");
        httppost.setURI(new URI(CCIMS_API));

        // execute & get the response.
        final HttpResponse response = httpClient.execute(httppost);
        final HttpEntity entity = response.getEntity();
        return JsonParser.parseString(IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8.displayName())).getAsJsonObject();
    }


}
