package net.seanomik.tamablefoxes.versions;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class FieldHelper {

    private static final VarHandle MODIFIERS;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
            MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void makeNonFinal(Field field) {
        int mods = field.getModifiers();
        if (Modifier.isFinal(mods)) {
            MODIFIERS.set(field, mods & ~Modifier.FINAL);
        }
    }

    public static void setField(Field field, Object obj, Object value) throws IllegalAccessException {
        makeNonFinal(field);
        field.setAccessible(true);
        field.set(obj, value);
        field.setAccessible(false);
    }
}