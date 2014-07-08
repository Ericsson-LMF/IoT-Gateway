package com.ericsson.commonutil.serialization;

/**
 * Holder for all Jackson Views. All of them should be interfaces and contain
 * Ignore class that implements all other views to allow exclusion which Jackson
 * currently doesn't support.
 *
 * @author aopkarja
 */
public interface View {

    /**
     * Used as View to whitelist identification
     */
    public interface ID {

        /**
         * Used as View to blacklist identification
         */
        public static class Ignore implements Stateless {
        }
    }

    public interface Stateless {

        public static class Ignore implements ID {
        }
    }

    public interface StatelessID extends ID, Stateless {

        public static class Ignore {

        }
    }
}
