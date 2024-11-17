package redot.executor.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public class Extensions {

    @SafeVarargs
    public static <T, C extends Collection<T>> C with(C in, T... toAdd) {
        final Collection<T> additionList = Arrays.asList(toAdd);
        in.addAll(additionList);
        return in;
    }

    public static <T> Optional<T> optional(T in) {
        return Optional.ofNullable(in);
    }

    public static <T> boolean isNull(T in) {
        return in == null;
    }

    public static <T> T ifNull(T in, T returnObject) {
        return in != null ? in : returnObject;
    }

    public static <T> void consume(T in, Consumer<T> action) {
        action.accept(in);
    }

}
