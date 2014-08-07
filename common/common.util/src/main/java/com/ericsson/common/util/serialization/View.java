package com.ericsson.common.util.serialization;

/**
 * Holder for all Jackson Views. All of them should be interfaces and contain
 * Ignore class that implements all other views to allow exclusion which Jackson
 * currently doesn't support.
 *
 * @author delma
 */
public interface View {

    /**
     * Used as View to whitelist identification
     */
    interface ID {

        /**
         * Used as View to blacklist identification
         */
        class Ignore implements Stateless {
        }
    }

    interface Stateless {

        class Ignore implements ID {
        }
    }

    interface StatelessID extends ID, Stateless {

        class Ignore {
        }
    }
}
