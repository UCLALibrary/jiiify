
package info.freelibrary.jiiify.image;

import java.io.File;
import java.io.FilenameFilter;

import info.freelibrary.util.FileUtils;

/**
 * @author kevin
 */
public class PairtreeRenamer {

    private PairtreeRenamer() {
    }

    /**
     * Quick and stupid script
     *
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Please supply a pairtree location, pattern (e.g. +), and replacement (e.g. ~)");
            System.out.println("  " + PairtreeRenamer.class.getName() + " \"/path/to/pairtree\" \"+\" \"~\"");
        }

        final String pattern = args[0];
        final String replacement = args[1];

        for (final File file : FileUtils.listFiles(new File(args[0]), new PairtreeFilter(pattern), true)) {
            if (file.renameTo(new File(file.getParentFile(), replace(file.getName(), pattern, replacement)))) {
                System.out.println(file);
            }
        }
    }

    private static String replace(final String aString, final String aPattern, final String aReplacement) {
        return aString.replace(aPattern, aReplacement);
    }

    static class PairtreeFilter implements FilenameFilter {

        private final String myPattern;

        public PairtreeFilter(final String aPattern) {
            myPattern = aPattern;
        }

        @Override
        public boolean accept(final File aDir, final String aFileName) {
            return aFileName.contains(myPattern) ? true : false;
        }

    }
}
