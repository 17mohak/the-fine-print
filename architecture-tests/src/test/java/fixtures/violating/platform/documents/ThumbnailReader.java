package fixtures.violating.platform.documents;

import javax.imageio.ImageReader;

/** Violates the worker boundary: parses untrusted image bytes in the Java core. */
public class ThumbnailReader {
    ImageReader reader;
}
