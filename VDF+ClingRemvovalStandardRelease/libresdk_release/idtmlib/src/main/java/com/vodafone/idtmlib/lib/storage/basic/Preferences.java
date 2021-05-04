package com.vodafone.idtmlib.lib.storage.basic;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Generic portable Shared Preferences helper class
 */
@Singleton
public class Preferences {
    private SharedPreferences preferences;
    private Type stringListType;
    private Type integerListType;
    private Type stringMapType;
    private Type integerMapType;

    @Inject
    public Preferences(SharedPreferences preferencesInstance) {
        this.preferences = preferencesInstance;
        this.stringListType = new TypeToken<List<String>>(){}.getType();
        this.integerListType = new TypeToken<List<Integer>>(){}.getType();
        this.stringMapType = new TypeToken<Map<String, String>>(){}.getType();
        this.integerMapType = new TypeToken<Map<String, Integer>>(){}.getType();
    }

    public void clearAllPreferences() {
        preferences.edit().clear().commit();
    }

    public boolean getBoolean(String name) {
        return preferences.getBoolean(name, false);
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        return preferences.getBoolean(name, defaultValue);
    }

    public int getInt(String name, int defValue) {
        return preferences.getInt(name, defValue);
    }

    public float getFloat(String name, float defValue) {
        return preferences.getFloat(name, defValue);
    }

    public long getLong(String name, long defValue) {
        return preferences.getLong(name, defValue);
    }

    public String getString(String name) {
        return preferences.getString(name, null);
    }

    public Set<String> getStringSet(String name) {
        return preferences.getStringSet(name, null);
    }

    public <T extends Enum<T>> T getEnum(Class<T> type, String name, T defValue) {
        String enumString = preferences.getString(name, defValue.name());
        return Enum.valueOf(type, enumString);
    }

    public List<String> getStringList(String name) {
        return getList(stringListType, name);
    }

    public List<String> getIntegerList(String name) {
        return getList(integerListType, name);
    }

    public <E> List<E> getList(Type type, String name) {
        String mapString = preferences.getString(name, null);
        return new Gson().fromJson(mapString, type);
    }

    public Map<String, String> getStringMap(String name) {
        return getMap(stringMapType, name);
    }

    public Map<String, Integer> getIntegerMap(String name) {
        return getMap(integerMapType, name);
    }

    public <K, V> Map<K, V> getMap(Type type, String name) {
        String mapString = preferences.getString(name, null);
        return new Gson().fromJson(mapString, type);
    }

    public <T> T get(Class<T> cls, String name) {
        String mapString = preferences.getString(name, null);
        return new Gson().fromJson(mapString, cls);
    }

    public void set(String name, Object value) {
        set(Collections.singletonMap(name, value));
    }

    public void set(Map<String, Object> nameValuePairs) {
        SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<String, Object> nameValuePair : nameValuePairs.entrySet()) {
            String name = nameValuePair.getKey();
            Object value = nameValuePair.getValue();
            if (value == null) {
                editor.remove(name);
            } else if (value instanceof Boolean) {
                editor.putBoolean(name, (boolean) value);
            } else if (value instanceof Integer) {
                editor.putInt(name, (int) value);
            } else if (value instanceof Float) {
                editor.putFloat(name, (float) value);
            } else if (value instanceof Long) {
                editor.putLong(name, (long) value);
            } else if (value instanceof String) {
                editor.putString(name, (String) value);
            } else if (value instanceof Set) {
                editor.putStringSet(name, (Set) value);
            } else if (value instanceof Enum) {
                editor.putString(name, ((Enum) value).name());
            } else {
                editor.putString(name, value.toString());
            }
        }
        editor.commit();
    }

    public int increment(String name, int defValue) {
        int val = getInt(name, defValue);
        if (val < Integer.MAX_VALUE) {
            val++;
        }
        set(name, val);
        return val;
    }

    public int decrement(String name, int defValue) {
        int val = getInt(name, defValue);
        if (val > Integer.MIN_VALUE) {
            val--;
        }
        set(name, val);
        return val;
    }

    public void remove(String... names) {
        for (String name : names) {
            set(name, null);
        }
    }
}
