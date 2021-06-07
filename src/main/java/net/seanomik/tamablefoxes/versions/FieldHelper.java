package net.seanomik.tamablefoxes.versions;

import net.seanomik.tamablefoxes.Utils;
import net.seanomik.tamablefoxes.io.LanguageConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.lang.invoke.MethodHandles;
//import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class FieldHelper {
    public static void makeNonFinal(Field field) {
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
    }

    public static void setField(Field field, Object obj, Object value) throws IllegalAccessException {
        makeNonFinal(field);
        field.setAccessible(true);
        field.set(obj, value);
        field.setAccessible(false);
    }
}