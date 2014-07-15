package com.ericsson.commonutil;

import com.ericsson.commonutil.function.FunctionalUtil;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author aopkarja
 */
public enum LegacyUtil {

    /**
     * Singleton.
     */
    INSTANCE;

    public static <T> Enumeration<T> toEnumeration(Iterator<T> iterator) {
        return new Enumeration<T>() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public T nextElement() {
                return iterator.next();
            }

        };
    }

    public static <K, V> Dictionary<K, V> toDictionary(Map<K, V> map) {
        return FunctionalUtil.applyIfCan(DictionaryWrapper.class, map, m -> m.dictionary).orElse(new MapWrapper(map));
    }

    public static <K, V> Map<K, V> toMap(Dictionary dictionary) {
        return FunctionalUtil.applyIfCan(MapWrapper.class, dictionary, d -> d.map).orElse(new DictionaryWrapper(dictionary));
    }

    private static class MapWrapper<K, V> extends Dictionary<K, V> {

        private final Map<K, V> map;

        public MapWrapper(Map<K, V> map) {
            this.map = map;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public Enumeration<K> keys() {
            return toEnumeration(map.keySet().iterator());
        }

        @Override
        public Enumeration<V> elements() {
            return toEnumeration(map.values().iterator());
        }

        @Override
        public V get(Object key) {
            return map.get((K) key);
        }

        @Override
        public V put(K key, V value) {
            return map.put(key, value);
        }

        @Override
        public V remove(Object key) {
            return map.remove((K) key);
        }
    }

    private static class DictionaryWrapper<K, V> extends AbstractMap<K, V> {

        private final Dictionary dictionary;

        public DictionaryWrapper(Dictionary dictionary) {
            this.dictionary = dictionary;
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            return new AbstractSet<Map.Entry<K, V>>() {

                @Override
                public Iterator<Map.Entry<K, V>> iterator() {
                    return new Iterator<Map.Entry<K, V>>() {
                        Enumeration enumeration = dictionary.keys();

                        @Override
                        public boolean hasNext() {
                            return enumeration.hasMoreElements();
                        }

                        @Override
                        public Map.Entry<K, V> next() {
                            return new Map.Entry<K, V>() {
                                Object key = enumeration.nextElement();
                                Object value = dictionary.get(key);

                                @Override
                                public K getKey() {
                                    return (K) key;
                                }

                                @Override
                                public V getValue() {
                                    return (V) value;
                                }

                                @Override
                                public V setValue(V value) {
                                    return (V) dictionary.put(key, value);
                                }

                            };
                        }

                    };
                }

                @Override
                public int size() {
                    return dictionary.size();
                }

            };
        }
    }
}
