package net.seanomik.tamablefoxes.versions;

import net.seanomik.tamablefoxes.Utils;
import net.seanomik.tamablefoxes.io.LanguageConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class FieldHelper {

    private static final VarHandle MODIFIERS;

    static {
        String version = System.getProperty("java.version");
        if (!version.startsWith("1.8")) {
            try {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
                MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);
            } catch (IllegalAccessException | NoSuchFieldException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            MODIFIERS = null;
        }
    }


    public static void makeNonFinal(Field field) {
        // Check if we're running a supported java version for this new method.
        if (MODIFIERS == null) {
            try {
                if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
                    Field fieldMutable = field.getClass().getDeclaredField("modifiers");
                    fieldMutable.setAccessible(true);
                    fieldMutable.set(field, fieldMutable.getInt(field) & ~Modifier.FINAL);
                    fieldMutable.setAccessible(false);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getFailureReplace());
                e.printStackTrace();
            }
        } else {
            int mods = field.getModifiers();
            if (Modifier.isFinal(mods)) {
                MODIFIERS.set(field, mods & ~Modifier.FINAL);
            }
        }
    }

    public static void setField(Field field, Object obj, Object value) throws IllegalAccessException {
        makeNonFinal(field);
        field.setAccessible(true);
        field.set(obj, value);
        field.setAccessible(false);
    }
}