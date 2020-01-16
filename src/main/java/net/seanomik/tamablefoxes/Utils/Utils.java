package net.seanomik.tamablefoxes.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {
    public static <K, V> List<K> getAllKeysForValue(Map<K, V> mapOfWords, V value)  {
        List<K> listOfKeys = null;

        if(mapOfWords.containsValue(value))  {
            listOfKeys = new ArrayList<>();

            // Iterate over each entry of map using entrySet
            for (Map.Entry<K, V> entry : mapOfWords.entrySet()) {
                // Check if value matches with given value
                if (entry.getValue().equals(value))
                {
                    // Store the key from entry to the list
                    listOfKeys.add(entry.getKey());
                }
            }
        }
        // Return the list of keys whose value matches with given value.
        return listOfKeys;
    }
}
