package com.ericsson.research.commonutil.function;

import java.util.concurrent.Callable;

/**
 *
 * @author delma
 */
public enum FunctionalUtil {

    /**
     * Singleton.
     */
    INSTANCE;

    /**
     * Wraps exception thrown to RuntimeException to work around current
     * limitations with checked exceptions in Functional Interfaces of Java 8.
     *
     * To throw exception wrapped in RuntimeException:
     * <blockquote><pre>
     * try{
     *   doSomething(() -> {
     *     smuggle(() -> throw new Exception());
     *   });
     * }catch(RuntimeException exception){
     *   throw (Exception) exception.getCause();
     * }
     * </pre></blockquote>
     *
     * @param <T> return type of callable
     * @param callable wrapper of exception throwing method call
     * @return what callable returns
     */
    public static <T> T smuggle(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
