package net.seanomik.tamablefoxes.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

@SuppressWarnings("removal")
public final class FieldHelper {
    public static void setFieldUsingUnsafe(final Field field, final Object object, final Object newValue) {
        try {
            field.setAccessible(true);
            int fieldModifiersMask = field.getModifiers();
            boolean isFinalModifierPresent = (fieldModifiersMask & Modifier.FINAL) == Modifier.FINAL;
            if (isFinalModifierPresent) {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    try {
                        sun.misc.Unsafe unsafe = getUnsafe();
                        long offset = unsafe.objectFieldOffset(field);
                        setFieldUsingUnsafe(object, field.getType(), offset, newValue, unsafe);
                        return null;
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                });
            } else {
                try {
                    field.set(object, newValue);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static sun.misc.Unsafe getUnsafe() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (sun.misc.Unsafe) field.get(null);
    }

    private static void setFieldUsingUnsafe(Object base, Class<?> type, long offset, Object newValue, Unsafe unsafe) {
        if (type == Integer.TYPE) {
            unsafe.putInt(base, offset, ((Integer) newValue));
        } else if (type == Short.TYPE) {
            unsafe.putShort(base, offset, ((Short) newValue));
        } else if (type == Long.TYPE) {
            unsafe.putLong(base, offset, ((Long) newValue));
        } else if (type == Byte.TYPE) {
            unsafe.putByte(base, offset, ((Byte) newValue));
        } else if (type == Boolean.TYPE) {
            unsafe.putBoolean(base, offset, ((Boolean) newValue));
        } else if (type == Float.TYPE) {
            unsafe.putFloat(base, offset, ((Float) newValue));
        } else if (type == Double.TYPE) {
            unsafe.putDouble(base, offset, ((Double) newValue));
        } else if (type == Character.TYPE) {
            unsafe.putChar(base, offset, ((Character) newValue));
        } else {
            unsafe.putObject(base, offset, newValue);
        }
    }
}