
package info.freelibrary.jiiify.image;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.imgscalr.Scalr;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.jiiify.iiif.ImageQuality;
import info.freelibrary.jiiify.iiif.ImageRegion;
import info.freelibrary.jiiify.iiif.ImageRegion.Region;
import info.freelibrary.jiiify.iiif.ImageRotation;
import info.freelibrary.jiiify.iiif.ImageSize;
import info.freelibrary.util.IOUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.buffer.Buffer;

/**
 * A native Java image object.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class JavaImageObject implements ImageObject {

    static {
        ImageIO.setUseCache(true);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaImageObject.class, MESSAGES);

    private BufferedImage myImage;

    /**
     * Creates new image using the pure Java image processing.
     *
     * @param aByteArray A source image in an array of bytes
     * @throws IOException If there is trouble reading the image file
     */
    public JavaImageObject(final byte[] aByteArray) throws IOException {
        final ByteArrayInputStream inStream = new ByteArrayInputStream(aByteArray);

        myImage = ImageIO.read(inStream);

        if (myImage == null) {
            LOGGER.error(MessageCodes.EXC_038);
            throw new IOException(LOGGER.getMessage(MessageCodes.EXC_072));
        }

        IOUtils.closeQuietly(inStream);
    }

    @Override
    public void extractRegion(final ImageRegion aRegion) throws IOException {
        final int x = aRegion.getInt(Region.X);
        final int y = aRegion.getInt(Region.Y);
        final int width = aRegion.getInt(Region.WIDTH);
        final int height = aRegion.getInt(Region.HEIGHT);

        LOGGER.debug(MessageCodes.DBG_084, myImage, x, y, width, height);
        myImage = Scalr.crop(myImage, x, y, width, height, Scalr.OP_ANTIALIAS);
    }

    @Override
    public void resize(final ImageSize aSize) throws IOException {
        if (!aSize.isFullSize()) {
            final int height = aSize.getHeight(myImage.getHeight(), myImage.getWidth());
            final int width = aSize.getWidth(myImage.getWidth(), myImage.getHeight());

            LOGGER.debug(MessageCodes.DBG_085, myImage, width, height);
            myImage = Scalr.resize(myImage, width, height, Scalr.OP_ANTIALIAS);
        }
    }

    @Override
    public void rotate(final ImageRotation aRotation) throws IOException {

    }

    @Override
    public void adjustQuality(final ImageQuality aQuality) throws IOException {

    }

    @Override
    public Buffer toBuffer(final String aFileExt) throws IOException {
        final String mimeType = ImageFormat.getMIMEType(aFileExt);
        final Iterator<ImageWriter> iterator = ImageIO.getImageWritersByMIMEType(mimeType);

        if (iterator.hasNext()) {
            final ImageWriter writer = iterator.next();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ImageOutputStream outStream = ImageIO.createImageOutputStream(baos);
            final ImageWriteParam param = writer.getDefaultWriteParam();

            if (param.canWriteProgressive()) {
                LOGGER.debug(MessageCodes.DBG_119, aFileExt);
                param.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
            }

            try {
                writer.setOutput(outStream);
                // TODO: Keep metadata, too
                writer.write(null, new IIOImage(myImage, null, null), param);
                outStream.flush();
                return Buffer.buffer(baos.toByteArray());
            } finally {
                outStream.close();
                writer.dispose();
            }
        } else {
            throw new IOException(LOGGER.getMessage(MessageCodes.EXC_071, mimeType));
        }
    }

    @Override
    public int getWidth() {
        return myImage.getWidth();
    }

    @Override
    public int getHeight() {
        return myImage.getHeight();
    }

    @Override
    public void free() {
        myImage.flush();
        myImage.getGraphics().dispose();
    }
}
