package com.ericsson.common.util.function;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collector.Characteristics.UNORDERED;

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

    /**
     * Wraps Runnable to TimerTask
     *
     * @param runnable Runnable
     * @return TimerTask
     */
    public static TimerTask timerTask(Runnable runnable) {
        return new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }

    /**
     * Puts to map cleaning what there was before if any.
     *
     * @param <K>
     * @param <V>
     * @param map
     * @param key
     * @param value
     * @param cleaner
     * @return
     */
    public static <K, V> V putAndClean(Map<K, V> map, K key, V value, Consumer<V> cleaner) {
        return map.compute(key, (k, v) -> {
            if (v != null) {
                cleaner.accept(v);
            }
            return value;
        });
    }

    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> entryCollector() {
        return new EntryCollector<>();
    }

    private static class EntryCollector<K, V> implements Collector<Map.Entry<K, V>, Map<K, V>, Map<K, V>> {

        @Override
        public Supplier<Map<K, V>> supplier() {
            return ConcurrentHashMap::new;
        }

        @Override
        public BiConsumer<Map<K, V>, Map.Entry<K, V>> accumulator() {
            return (m, e) -> m.put(e.getKey(), e.getValue());
        }

        @Override
        public BinaryOperator<Map<K, V>> combiner() {
            return (a, b) -> a;
        }

        @Override
        public Function<Map<K, V>, Map<K, V>> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return EnumSet.of(CONCURRENT, IDENTITY_FINISH, UNORDERED);
        }
    }
}
