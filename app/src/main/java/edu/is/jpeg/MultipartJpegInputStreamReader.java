package edu.is.jpeg;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import edu.is.StreamSearcher;

/**
 * Consumes an input stream assuming it a {@code multipart/x-mixed-replace} stream where each section is a JPEG image.
 */
public class MultipartJpegInputStreamReader {
    
    /**
     * Start sequence of a JPEG image.
     * See https://de.wikipedia.org/wiki/JPEG_File_Interchange_Format
     */
    public static final byte[] JPEG_START_OF_IMAGE = new byte[] { (byte) 0xff, (byte) 0xd8 };
    
    /**
     * End sequence of a JPEG image.
     * See https://de.wikipedia.org/wiki/JPEG_File_Interchange_Format
     */
    public static final byte[] JPEG_END_OF_IMAGE = new byte[] { (byte) 0xff, (byte) 0xd9 };
    
    private final StreamSearcher searcher;
    private final InputStream inputStream;

    private byte[] nextPattern;
    
    public MultipartJpegInputStreamReader(InputStream inputStream, byte[] boundaryPattern) {
        this.searcher = new StreamSearcher(boundaryPattern);
        this.nextPattern = boundaryPattern;
        this.inputStream = inputStream;
    }

    /**
     * Reads the next image into the {@code buffer}.
     * @param buffer the buffer for the image
     * @throws IOException if reading from the stream fails or writing to the buffer fails
     */
    public int read(ByteBuffer buffer) throws IOException {
        while (searcher.search(inputStream) > 0) {
            if (nextPattern == JPEG_START_OF_IMAGE) {
                System.out.println("Hit start of image");
                buffer.put(nextPattern);
                searcher.setBuffer(buffer);
                setNextPattern(JPEG_END_OF_IMAGE);
            } else if (nextPattern == JPEG_END_OF_IMAGE) {                
                System.out.println("Hit end of image: " + buffer.position());
                                              
                setNextPattern(JPEG_START_OF_IMAGE);
                searcher.setBuffer(null);
                return buffer.position();
            } else {
                System.out.println("Hit boundary");
                setNextPattern(JPEG_START_OF_IMAGE);
                searcher.setBuffer(null);
            }
        }
        return -1;
    }
    
    protected void setNextPattern(byte[] nextPattern) {
        this.searcher.setPattern(nextPattern);
        this.nextPattern = nextPattern;
    }

}
