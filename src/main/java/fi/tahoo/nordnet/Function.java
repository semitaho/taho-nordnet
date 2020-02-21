package fi.tahoo.nordnet;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import fi.tahoo.nordnet.model.OmaOsake;
import fi.tahoo.nordnet.model.YleisnakymaResponse;

import java.util.List;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

  /**
   * This function listens at endpoint "/api/HttpTrigger-Java". Two ways to invoke it using "curl" command in bash:
   * 1. curl -d "HTTP Body" {your host}/api/HttpTrigger-Java&code={your function key}
   * 2. curl "{your host}/api/HttpTrigger-Java?name=HTTP%20Query&code={your function key}"
   * Function Key is not needed when running locally, it is used to invoke function deployed to Azure.
   * More details: https://aka.ms/functions_authorization_keys
   */
  @FunctionName("Yleisnakyma")
  public HttpResponseMessage run(
          @HttpTrigger(name = "req",  methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
          final ExecutionContext context) {
    context.getLogger().info("Java HTTP trigger processed a request.");
    NordnetRobot nordnetRobot = new NordnetRobot();
    YleisnakymaResponse yleisnakymaResponse = nordnetRobot.checkYleisnakyma();


    // Parse query parameter

        /*
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);



        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
         */
    return request.createResponseBuilder(HttpStatus.OK).body(yleisnakymaResponse).build();

  }
}
