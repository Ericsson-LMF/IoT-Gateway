package com.ericsson.commonutil;

/**
 * Utility class that gives fluent API for constructing path parser
 *
 * @author aopkarja
 */
public enum PathUtil {

    /**
     * Singleton.
     */
    INSTANCE;

    private static final String PATH_DELIMITER = "/";
    private static final PatherEnder ENDER = () -> {
        throw new PatherException();
    };

    /**
     * Starts building pather that applies pather if path starts right way
     *
     * @param start
     * @param pather
     * @return pather builder
     */
    public static PatherBuilder startsWith(String start, Pather pather) {
        return new PatherBuilder(start, pather);
    }

    /**
     * Starts building pather that applies pather holder if path starts right
     * way
     *
     * @param start
     * @param pather
     * @return pather builder
     */
    public static PatherBuilder startsWith(String start, PatherHolder pather) {
        return new PatherBuilder(start, pather);
    }

    /**
     * Starts building pather holder that applies pather if it can
     *
     * @param pather
     * @return pather holder
     */
    public static PatherHolder andThen(Pather pather) {
        return new PatherHolder(pather);
    }

    /**
     * Turn split pather to pather usable by builder
     *
     * @param pather split pather
     * @return pather
     */
    public static Pather splits(PatherSplitter pather) {
        return path -> {
            String[] split = path.split(PATH_DELIMITER, 2);
            return pather.apply(split[0], split.length > 1 ? split[1] : "");
        };
    }

    /**
     * Allows doing something if there is more of the path still left and
     * something else if there's not.
     */
    public static class PatherHolder {

        private PatherEnder otherwise = ENDER;
        private final Pather current;

        private PatherHolder(Pather pather) {
            this.current = pather;
        }

        /**
         * This ender will be applied if path ends here
         *
         * @param patherEnder
         * @return this
         */
        public PatherHolder otherwise(PatherEnder patherEnder) {
            otherwise = patherEnder;
            return this;
        }
    }

    /**
     * Builder for building Pathers to parse paths
     */
    public static class PatherBuilder {

        private PatherEnder otherwise = ENDER;
        private Pather current;

        private PatherBuilder(String start, PatherHolder pather) {
            current = createPather(start, pather.current, pather.otherwise, path -> otherwise.apply());
        }

        private PatherBuilder(String start, Pather pather) {
            current = createPather(start, pather, ENDER, path -> otherwise.apply());
        }

        /**
         * Alternative start for path
         *
         * @param start
         * @param pather
         * @return this
         */
        public PatherBuilder orBy(String start, Pather pather) {
            current = createPather(start, pather, ENDER, current);
            return this;
        }

        /**
         * Alternative start for path
         *
         * @param start
         * @param pather
         * @return this
         */
        public PatherBuilder orBy(String start, PatherHolder pather) {
            current = createPather(start, pather.current, pather.otherwise, current);
            return this;
        }

        /**
         * Helper function to construct pather that does checks to figure out
         * what pather gets applied
         *
         * @param start
         * @param current
         * @param otherwise
         * @param previous
         * @return pather
         */
        private Pather createPather(String start, Pather current, PatherEnder otherwise, Pather previous) {
            return path -> {
                if (path.startsWith(start)) {
                    String[] split = path.split(PATH_DELIMITER, 2);
                    if (split.length > 1 && !split[1].isEmpty()) {
                        return current.apply(split[1]);
                    }
                    return otherwise.apply();
                }
                return previous.apply(path);
            };
        }

        /**
         * This ender is applied if there's no starts applicable
         *
         * @param patherEnder
         * @return this
         */
        public PatherBuilder otherwise(PatherEnder patherEnder) {
            otherwise = patherEnder;
            return this;
        }

        /**
         * Builds the pather
         *
         * @return pather
         */
        public Pather build() {
            return current;
        }

        /**
         * Applies the pather right away
         *
         * @param path
         * @return string
         * @throws Exception
         */
        public String apply(String path) throws Exception {
            return current.apply(path);
        }
    }

    /**
     * Pather to do something that requires start and rest of the path
     */
    public interface PatherSplitter {

        String apply(String first, String rest) throws Exception;
    }

    /**
     * Pather to do something while parsing path
     */
    public interface Pather {

        String apply(String path) throws Exception;
    }

    /**
     * Pather to do something at the end of path
     */
    public interface PatherEnder {

        String apply() throws Exception;
    }

    /**
     * Exception thrown while building if path cannot be parsed
     */
    public static class PatherException extends Exception {
    }
}
