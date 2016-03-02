package org.maxur.justj.core.cli.info;

import org.maxur.justj.core.cli.annotation.Flag;
import org.maxur.justj.core.cli.annotation.Operands;
import org.maxur.justj.core.cli.annotation.Option;
import org.maxur.justj.core.cli.argument.Argument;
import org.maxur.justj.core.cli.exception.InvalidCommandArgumentException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>26.02.2016</pre>
 */
public abstract class CliOptionInfo extends CliItemInfo {

    private CliOptionInfo(final Field field) {
        super(field);
    }

    static CliOptionInfo option(final Field field) {
        return detectType(field);
    }

    static CliOptionInfo operand(final Field field) {
        return detectType(field);
    }

    private static CliOptionInfo detectType(final Field field) {
        if (isBoolean(field)) {
            return new FlagInfo(field);
        }
        if (field.getType().isEnum()) {
            return new TriggerInfo(field);
        }
        if (Collection.class.isAssignableFrom(field.getType())) {
            return new ListInfo(field);
        }
        if (field.isAnnotationPresent(Option.class) || field.isAnnotationPresent(Operands.class)) {
            return new OptionInfo(field);
        }

        return new NoneInfo(field);
    }

    private static boolean isBoolean(final Field field) {
        return field.getType() == boolean.class || field.getType() == Boolean.class;
    }

    @Override
    protected String findName() {
        if (field().isAnnotationPresent(Flag.class)) {
            final Flag flag = field().getAnnotation(Flag.class);
            return flag.value().isEmpty() ? field().getName() : flag.value();
        }
        if (field().isAnnotationPresent(Option.class)) {
            final Option option = field().getAnnotation(Option.class);
            return option.value().isEmpty() ? field().getName() : option.value();
        }
        return field().getName().toLowerCase();
    }


    protected Object valueFor(final Class<?> type, final String arg) {
        return type == String.class ?
            arg :
            makeValueByValueOf(type, arg);
    }

    private <T> T makeValueByValueOf(final Class<T> type, final String arg) {
        try {
            //noinspection unchecked
            return (T) valueOfMethod(type).invoke(null, arg);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(
                format("Arguments cannot be instantiated. Illegal value '%s' of %s", arg, type),
                e
            );
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                format("Arguments cannot be instantiated. Illegal access to type %s", type),
                e
            );
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                format("Arguments cannot be instantiated. Option '%s' has unsupported type", arg),
                e
            );
        }
    }

    private Method valueOfMethod(final Class<?> type) throws NoSuchMethodException {
        return type.getMethod("valueOf", String.class);
    }


    protected void setOption(
        final Argument argument,
        final Object value,
        final Object command
    ) throws InvalidCommandArgumentException {
        this.field().setAccessible(true);
        try {
            this.field().set(command, value);
        } catch (IllegalAccessException e) {
            throw new InvalidCommandArgumentException(
                this.name(),
                argument.asString(),
                format("Illegal access to field %s", this.field().getName())
                , e
            );
        }
    }

    abstract void apply(Argument argument, Object command) throws InvalidCommandArgumentException;

    private static class FlagInfo extends CliOptionInfo {
        private FlagInfo(final Field field) {
            super(field);
        }

        @Override
        void apply(final Argument argument, final Object command) throws InvalidCommandArgumentException {
            setOption(argument, true, command);
        }

    }

    private static class TriggerInfo extends CliOptionInfo {

        private final Set<CliOptionInfo> children = new HashSet<>();

        private TriggerInfo(final Field field) {
            super(field);
            this.children.addAll(findChildren(field));
        }

        private static Set<CliOptionInfo> findChildren(final Field field) {
            final Set<CliOptionInfo> result = new HashSet<>();
            for (Field f : field.getType().getDeclaredFields()) {
                if (f.isEnumConstant()) {
                    result.add(new NoneInfo(f));
                }
            }
            return result;
        }

        @Override
        public boolean applicable(final Argument argument) {
            return children.stream().anyMatch(child -> child.applicable(argument));
        }

        @Override
        void apply(final Argument argument, final Object command) throws InvalidCommandArgumentException {
            for (CliOptionInfo child : children) {
                final boolean result = child.applicable(argument);
                if (result) {
                    final Object value = valueFor(child.field().getType(), child.field().getName());
                    setOption(argument, value, command);
                }
            }
        }

    }

    private static class OptionInfo extends CliOptionInfo {
        private OptionInfo(final Field field) {
            super(field);
        }

        @Override
        void apply(final Argument argument, final Object command) throws InvalidCommandArgumentException {
            final Object value = valueFor(field().getType(), argument.optionArgument());
            setOption(argument, value, command);
        }

        @Override
        public boolean isOption() {
            return true;
        }
    }

    private static class ListInfo extends CliOptionInfo {

        private ListInfo(final Field field) {
            super(field);
        }

        @Override
        void apply(final Argument argument, final Object command) throws InvalidCommandArgumentException {
            final Collection<Object> value;
            try {
                this.field().setAccessible(true);
                //noinspection unchecked
                final Collection<Object> o1 = (Collection<Object>) field().get(command);
                value = o1 != null ? o1 : collectionFor(field());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                    format("Arguments cannot be instantiated. Illegal access to type %s", field().getType()),
                    e
                );
            }
            setOption(argument, value, command);
            ParameterizedType stringListType = (ParameterizedType) field().getGenericType();
            Class<?> itemType = (Class<?>) stringListType.getActualTypeArguments()[0];
            final String[] values = argument.optionArgument().split(",");
            Arrays.stream(values)
                .map(v -> valueFor(itemType, v))
                .forEach(o -> value.add(o));
        }

        private Collection<Object> collectionFor(final Field field) {
            if (Modifier.isAbstract(field.getType().getModifiers())) {
                if (Set.class.isAssignableFrom(field.getType())) {
                    return new HashSet<>();
                } else {
                    return new ArrayList<>();
                }
            } else {
                try {
                    return (Collection<Object>) field.getType().newInstance();
                } catch (InstantiationException e) {
                    throw new IllegalStateException(
                        format(
                            "Arguments cannot be instantiated. Default constructor of %s is unreachable",
                            field.getType()
                        ),
                        e
                    );
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(
                        format("Arguments cannot be instantiated. Illegal access to type %s", field.getType()),
                        e
                    );
                }
            }

        }

        @Override
        public boolean isList() {
            return true;
        }
    }

    private static class NoneInfo extends CliOptionInfo {
        private NoneInfo(final Field field) {
            super(field);
        }

        @Override
        void apply(final Argument argument, final Object command) throws InvalidCommandArgumentException {
        }

    }
}
