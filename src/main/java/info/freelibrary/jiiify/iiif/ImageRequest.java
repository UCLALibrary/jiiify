
package info.freelibrary.jiiify.iiif;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.StringJoiner;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.pairtree.PairtreeUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

/**
 * IIIF image request.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageRequest implements Cloneable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageRequest.class, MESSAGES);

    private static final String DELIM = "/";

    private static final String EMPTY = "";

    private final String myID;

    private final String myServicePrefix;

    private ImageRegion myRegion;

    private ImageSize mySize;

    private ImageRotation myRotation;

    private ImageQuality myQuality;

    private ImageFormat myFormat;

    /**
     * Creates a IIIF image request object for a particular image region.
     *
     * @param aID An ID for the image being requested
     * @param aServicePrefix A IIIF service prefix
     * @param aRegion An image region being requested
     */
    public ImageRequest(final String aID, final String aServicePrefix, final ImageRegion aRegion) {
        myRegion = aRegion;
        myID = aID;
        myServicePrefix = aServicePrefix.replace(DELIM, EMPTY);
        mySize = new ImageSize();
        myRotation = new ImageRotation();
        myQuality = new ImageQuality();
        myFormat = new ImageFormat();
    }

    /**
     * Creates a IIIF image request object for a particular image region and with a particular size.
     *
     * @param aID An ID for the image being requested
     * @param aServicePrefix A IIIF service prefix
     * @param aRegion An image region being requested
     * @param aSize A requested image size
     */
    public ImageRequest(final String aID, final String aServicePrefix, final ImageRegion aRegion,
            final ImageSize aSize) {
        myRegion = aRegion;
        myID = aID;
        myServicePrefix = aServicePrefix.replace(DELIM, EMPTY);
        mySize = aSize;
        myRotation = new ImageRotation();
        myQuality = new ImageQuality();
        myFormat = new ImageFormat();
    }

    /**
     * Creates a IIIF image request object for image with a particular size.
     *
     * @param aID An ID for the image being requested
     * @param aServicePrefix A IIIF service prefix
     * @param aSize A requested image size
     */
    public ImageRequest(final String aID, final String aServicePrefix, final ImageSize aSize) {
        mySize = aSize;
        myID = aID;
        myServicePrefix = aServicePrefix.replace(DELIM, EMPTY);
        myRegion = new ImageRegion();
        myRotation = new ImageRotation();
        myQuality = new ImageQuality();
        myFormat = new ImageFormat();
    }

    /**
     * Creates a IIIF image request object for a region from an image with a particular size, rotation, quality, and
     * format.
     *
     * @param aID An ID for the image being requested
     * @param aServicePrefix A IIIF service prefix
     * @param aRegion A region from the requested image
     * @param aSize A requested image size
     * @param aRotation A rotation to perform on the requested image region
     * @param aQuality A quality of image to return
     * @param aFormat A format of image to return
     */
    public ImageRequest(final String aID, final String aServicePrefix, final ImageRegion aRegion,
            final ImageSize aSize, final ImageRotation aRotation, final ImageQuality aQuality,
            final ImageFormat aFormat) {
        mySize = aSize;
        myID = aID;
        myServicePrefix = aServicePrefix.replace(DELIM, EMPTY);
        myRegion = aRegion;
        myRotation = aRotation;
        myQuality = aQuality;
        myFormat = aFormat;
    }

    /**
     * Takes a IIIF request in the form of /service-prefix/id/region/size/rotation/quality.format
     *
     * @param aIIIFImagePath A IIIF image path
     * @throws IIIFException If the supplied path is not valid
     */
    public ImageRequest(final String aIIIFImagePath) throws IIIFException {
        final String[] pathComponents = aIIIFImagePath.substring(1).split(DELIM);
        final int dotIndex = pathComponents[5].lastIndexOf(".");

        LOGGER.debug(MessageCodes.DBG_071, aIIIFImagePath);

        if (dotIndex == -1) {
            throw new IIIFException();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.DBG_072, pathComponents[0], PairtreeUtils.decodeID(pathComponents[1]),
                    pathComponents[2], pathComponents[3], pathComponents[4], pathComponents[5]);
        }

        myServicePrefix = pathComponents[0];
        myID = PathUtils.decode(pathComponents[1]);
        myRegion = new ImageRegion(pathComponents[2]);
        mySize = new ImageSize(pathComponents[3]);
        myRotation = new ImageRotation(pathComponents[4]);
        myQuality = new ImageQuality(pathComponents[5].substring(0, dotIndex));
        myFormat = new ImageFormat(pathComponents[5].substring(dotIndex + 1));
    }

    /**
     * Gets the IIIF service prefix at which the image request was made.
     *
     * @return The IIIF service prefix at which the image request was made
     */
    public String getPrefix() {
        return myServicePrefix;
    }

    /**
     * Gets the ID of the requested image.
     *
     * @return The ID of the requested image
     */
    public String getID() {
        return myID;
    }

    /**
     * Gets the desired region of the requested image.
     *
     * @return The desired region of the requested image
     */
    public ImageRegion getRegion() {
        return myRegion;
    }

    /**
     * Resets the region for the image request.
     *
     * @param aImageRegion A new image region
     */
    public void setRegion(final ImageRegion aImageRegion) {
        myRegion = aImageRegion;
    }

    /**
     * Gets the size of the requested image.
     *
     * @return The size of the requested image
     */
    public ImageSize getSize() {
        return mySize;
    }

    /**
     * Resets the image size for the image request.
     *
     * @param aImageSize A new image size
     */
    public void setSize(final ImageSize aImageSize) {
        mySize = aImageSize;
    }

    /**
     * Gets the rotation of the requested image.
     *
     * @return The rotation of the requested image
     */
    public ImageRotation getRotation() {
        return myRotation;
    }

    /**
     * Resets the image rotation for the image request
     *
     * @param aImageRotation A new image rotation
     */
    public void setRotation(final ImageRotation aImageRotation) {
        myRotation = aImageRotation;
    }

    /**
     * Gets the quality of the requested image.
     *
     * @return The quality of the requested image
     */
    public ImageQuality getQuality() {
        return myQuality;
    }

    /**
     * Resets the quality for the image request.
     *
     * @param aImageQuality A new image quality
     */
    public void setQuality(final ImageQuality aImageQuality) {
        myQuality = aImageQuality;
    }

    /**
     * Gets the format of the requested image.
     *
     * @return The format of the requested image
     */
    public ImageFormat getFormat() {
        return myFormat;
    }

    /**
     * Resets the format for the image request.
     *
     * @param aImageFormat A new image format
     */
    public void setFormat(final ImageFormat aImageFormat) {
        myFormat = aImageFormat;
    }

    @Override
    public String toString() {
        final String fileName = StringUtils.toString('.', myQuality, myFormat);
        final String id = getPathEncodedID();

        return new StringJoiner(DELIM, DELIM, EMPTY).add(myServicePrefix).add(id).add(myRegion.toString()).add(mySize
                .toString()).add(myRotation.toString()).add(fileName).toString();
    }

    /**
     * Returns a Pairtree object path for this image request.
     *
     * @return A Pairtree object path for this image request
     */
    public String getPath() {
        final String fileName = StringUtils.toString('.', myQuality, myFormat);
        return Paths.get(myRegion.toString(), mySize.toString(), myRotation.toString(), fileName).toString();
    }

    /**
     * Determines whether the supplied object is an ImageRequest that is the same as the supplied one.
     */
    @Override
    public boolean equals(final Object aObject) {
        if (aObject instanceof ImageRequest) {
            final ImageRequest request = (ImageRequest) aObject;
            final String servicePrefix = request.getPrefix();
            final String id = request.getID();
            final ImageFormat format = request.getFormat();
            final ImageQuality quality = request.getQuality();
            final ImageRegion region = request.getRegion();
            final ImageRotation rotation = request.getRotation();
            final ImageSize size = request.getSize();

            if (servicePrefix.equals(myServicePrefix) && id.equals(myID) && format.equals(myFormat) && quality.equals(
                    myQuality) && region.equals(myRegion) && rotation.equals(myRotation) && size.equals(mySize)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 13;

        result += myServicePrefix.hashCode();
        result += myID.hashCode();
        result += myFormat.hashCode();
        result += myQuality.hashCode();
        result += myRegion.hashCode();
        result += myRotation.hashCode();
        result += mySize.hashCode();

        return 37 * result;
    }

    /**
     * Creates a clone of the ImageRequest.
     */
    @Override
    public ImageRequest clone() {
        try {
            return (ImageRequest) super.clone();
        } catch (final CloneNotSupportedException details) {
            throw new RuntimeException(details);
        }
    }

    /**
     * A simple convenience method to return the path encoded ID.
     *
     * @return The path encoded ID
     */
    private String getPathEncodedID() {
        try {
            return PathUtils.encodeIdentifier(myID);
        } catch (final URISyntaxException details) {
            LOGGER.warn(MessageCodes.WARN_014, myID);
            return myID;
        }
    }
}
