package com.ericsson.commonutil.function;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class to help with functional interfaces.
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

    /**
     * Executes code if target object is of certain type. Code doesn't get
     * executed if object is null.
     *
     * Simplifies code
     * <blockquote><pre>
     * if(object instanceof TypeThatWeWantItToBe) {
     *   TypeThatWeWantItToBe temp = (TypeThatWeWantItToBe) object;
     *   temp.doSomething();
     * }
     * </pre></blockquote>
     * to
     * <blockquote><pre>
     * FunctionalUtil.doIfCan(TypeThatWeWantItToBe.class, object, temp -> {
     *   temp.doSomething();
     * });
     * </pre></blockquote>
     *
     * @param <T> Type we want it to be
     * @param <K> Return type
     * @param type Needed because Type erasure
     * @param object Object we want to do something on
     * @param code code that needs to be executed
     * @return result
     */
    public static <T, K> Optional<K> applyIfCan(Class<T> type, Object object, Function<T, K> code) {
        if (type.isInstance(object)) {
            return Optional.ofNullable(code.apply((T) object));
        }
        return Optional.empty();
    }

    public static <T> void acceptIfCan(Class<T> type, Object object, Consumer<T> code) {
        if (type.isInstance(object)) {
            code.accept((T) object);
        }
    }
}
