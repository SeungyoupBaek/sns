package com.youp.sns.util;

import java.util.Optional;

public class ClassUtils {

    private ClassUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> Optional<T> getSafeCastInstance(Object o, Class<T> clazz) {
        return clazz != null && clazz.isInstance(o) ? Optional.of(clazz.cast(o)) : Optional.empty();
    }
}
