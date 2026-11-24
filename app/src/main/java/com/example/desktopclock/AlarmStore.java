package com.example.desktopclock;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public final class AlarmStore {
    private static final String PREFS = "alarms_store";
    private static final String KEY = "alarms_json";

    private AlarmStore() {}

    public static List<Alarm> load(Context c) {
        List<Alarm> list = new ArrayList<>();
        String s = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]");
        try {
            JSONArray arr = new JSONArray(s);
            for (int i = 0; i < arr.length(); i++) list.add(Alarm.fromJson(arr.getJSONObject(i)));
        } catch (JSONException ignored) { }
        return list;
    }

    public static void save(Context c, List<Alarm> list) {
        JSONArray arr = new JSONArray();
        try {
            for (Alarm a : list) arr.put(a.toJson());
        } catch (JSONException ignored) { }
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply();
    }

    public static Alarm find(Context c, long id) {
        for (Alarm a : load(c)) if (a.id == id) return a;
        return null;
    }

    public static void update(Context c, Alarm updated) {
        List<Alarm> list = load(c);
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).id == updated.id) list.set(i, updated);
        save(c, list);
    }
}
