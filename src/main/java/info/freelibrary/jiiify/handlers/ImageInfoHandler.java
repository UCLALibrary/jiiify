
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.MESSAGES;
import static info.freelibrary.jiiify.Metadata.DEFAULT_CACHE_CONTROL;
import static info.freelibrary.jiiify.Metadata.JSON_MIME_TYPE;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_HEADER;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import java.net.URISyntaxException;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.iiif.ImageInfo;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * A handler that handles requests for image info JSON files.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageInfoHandler extends JiiifyHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageInfoHandler.class, MESSAGES);

    /**
     * Creates a handler that handles requests for image info JSON files.
     *
     * @param aConfig A configuration object
     */
    public ImageInfoHandler(final Configuration aConfig) {
        super(aConfig);
    }

    /**
     * Handles image info requests (mapped to: /service-prefix/[ID]/info.json).
     */
    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response();
        final HttpServerRequest request = aContext.request();
        final String id = PathUtils.decode(request.uri().split("\\/")[2]);
        final PairtreeObject ptObj = myConfig.getDataDir(id).getObject(id);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.DBG_035, ptObj.getPath(ImageInfo.FILE_NAME));
        }

        // FIXME: put this centrally for all IIIF routes(?)
        response.headers().set("Access-Control-Allow-Origin", "*");

        ptObj.find(ImageInfo.FILE_NAME, existsResult -> {
            if (existsResult.succeeded()) {
                if (existsResult.result()) {
                    ptObj.get(ImageInfo.FILE_NAME, getResult -> {
                        if (getResult.succeeded()) {
                            final JsonObject json = getResult.result().toJsonObject();
                            final String server = myConfig.getServer() + myConfig.getServicePrefix();
                            final Buffer buffer;

                            try {
                                json.put(ImageInfo.ID, server + "/" + PathUtils.encodeIdentifier(id));
                            } catch (final URISyntaxException details) {
                                LOGGER.error(details, MessageCodes.EXC_050, id);
                            }

                            buffer = Buffer.buffer(json.toString());

                            response.putHeader(Metadata.CONTENT_LENGTH, Integer.toString(buffer.length()));
                            response.putHeader(Metadata.CACHE_CONTROL, DEFAULT_CACHE_CONTROL);
                            response.putHeader(Metadata.CONTENT_TYPE, JSON_MIME_TYPE);
                            response.end(buffer);
                            response.close();

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug(MessageCodes.DBG_036, request.uri());
                            }
                        } else {
                            fail(aContext, existsResult.cause());
                            error(aContext, request);
                        }
                    });
                } else {
                    aContext.fail(404);
                    aContext.put(ERROR_HEADER, "Image Info File Not Found");
                    aContext.put(ERROR_MESSAGE, msg("Image info file not found: " + request.uri()));
                }
            } else {
                fail(aContext, existsResult.cause());
                error(aContext, request);
            }
        });
    }

    private void error(final RoutingContext aContext, final HttpServerRequest aRequest) {
        aContext.put(ERROR_HEADER, "Image Info Request Error");
        aContext.put(ERROR_MESSAGE, msg("Failed to serve image info: {}", aRequest.uri()));
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
