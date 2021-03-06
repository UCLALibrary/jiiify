
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.verticles.JiiifyMainVerticle;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.web.RoutingContext;

/**
 * A handler that handles status requests.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class StatusHandler extends JiiifyHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusHandler.class, MESSAGES);

    /* The critical flag as expected by our Nagios script */
    private static final String CRITICAL = "CRITICAL_";

    /* The okay flag as expected by our Nagios script */
    private static final String OK = "OK_";

    /* The unknown flag as expected by our Nagios script */
    private static final String UNKNOWN = "UNKNOWN_";

    /* Count is one of the out of the box metrics */
    private static final String COUNT = "count";

    /**
     * Creates a status handler.
     *
     * @param aConfig The application's configuration
     */
    public StatusHandler(final Configuration aConfig) {
        super(aConfig);
    }

    // FIXME: There is now a standard status checker in Vert.x we should use instead.

    @Override
    public void handle(final RoutingContext aContext) {
        final String mainVerticle = "vertx.verticles." + JiiifyMainVerticle.class.getName();
        final MetricsService metricsService = MetricsService.create(aContext.vertx());
        final JsonObject metrics = metricsService.getMetricsSnapshot(aContext.vertx());
        final String[] pathParts = aContext.request().path().split("\\/");
        final String statusCheck = pathParts[pathParts.length - 1];
        final HttpServerResponse response = aContext.response();

        if ("basic".equals(statusCheck)) {
            switch (metrics.getJsonObject(mainVerticle, new JsonObject().put(COUNT, -1)).getInteger(COUNT)) {
                case 0:
                    response.end(CRITICAL + "Jiiify Main Verticle is dead");
                    break;
                case 1:
                    response.end(OK + "Jiiify Main Verticle is alive");
                    break;
                default:
                    response.end(UNKNOWN + "Jiiify Main Verticle state is unknown");
            }
        }

        response.close();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
