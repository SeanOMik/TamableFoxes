package net.seanomik.tamablefoxes;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static boolean isTamableFox(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle().getClass().getName().contains("TamableFox") || ((CraftEntity) entity).getHandle() instanceof EntityTamableFox;
    }

    public static String getPrefix() {
        return ChatColor.RED + "[Tamable Foxes] ";
    }

    public static Object getPrivateFieldValue(Class c, String field, Object instance) {
        Object value = null;
        try {
            Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            value = f.get(instance);
            f.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    public static void setPrivateFieldValue(Class c, String field, Object instance, Object value) {
        try {
            Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            f.set(instance, value);
            f.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendConsoleMessage(String message) {
        TamableFoxes.getPlugin().getServer().getConsoleSender().sendMessage(message);
    }

    public static Class<?> getPrivateInnerClass(Class outer, String innerName) {
        for (Class<?> declaredClass : outer.getDeclaredClasses()) {
            if (declaredClass.getSimpleName().equals(innerName)) return declaredClass;
        }

        return null;
    }

    public static Object instantiatePrivateInnerClass(Class outer, String innerName, Object outerObject, List<Object> args, List<Class<?>> argTypes) {
        try {
            Class<?> innerClass = getPrivateInnerClass(outer, innerName);

            Object[] argObjects = new Object[args.size()+1];
            Class<?>[] argClasses = new Class<?>[argTypes.size()+1];

            // Needed due to how List#toArray() converts the classes to objects
            for (int i = 0; i < argClasses.length; i++) {
                if (i == argClasses.length-1) continue;
                argObjects[i+1] = args.get(i);
                argClasses[i+1] = argTypes.get(i);
            }
            argObjects[0] = outerObject;
            argClasses[0] = outer;

            Constructor<?> innerConstructor = innerClass.getDeclaredConstructor(argClasses);
            innerConstructor.setAccessible(true);

            Object instantiatedClass = innerConstructor.newInstance(argObjects);

            innerConstructor.setAccessible(false);

            return instantiatedClass;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
