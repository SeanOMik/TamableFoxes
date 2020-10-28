package net.seanomik.tamablefoxes;

import org.bukkit.ChatColor;

import java.lang.reflect.Constructor;
import java.util.List;

public class Utils {
    public static String getPrefix() {
        return ChatColor.RED + "[Tamable Foxes] ";
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

            Object[] argObjects = new Object[args.size() + 1];
            Class<?>[] argClasses = new Class<?>[argTypes.size() + 1];

            // Needed due to how List#toArray() converts the classes to objects
            for (int i = 0; i < argClasses.length; i++) {
                if (i == argClasses.length - 1) continue;
                argObjects[i + 1] = args.get(i);
                argClasses[i + 1] = argTypes.get(i);
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
