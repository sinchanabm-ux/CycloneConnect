package com.example.cycloneconnect.data

import android.content.Context
import org.json.JSONArray
import java.util.Locale

// One searchable coastal place.
data class CoastalCity(val name: String, val state: String) {
    val label: String get() = "$name, $state"
}

// Loads assets/coastal_cities.json once and answers autocomplete queries.
// To use a different dataset, replace that JSON file (keep the "city" and "state" keys).
object CoastalCities {

    @Volatile
    private var cache: List<CoastalCity>? = null

    fun load(context: Context): List<CoastalCity> =
        cache ?: synchronized(this) {
            cache ?: parse(context.applicationContext).also { cache = it }
        }

    private fun parse(context: Context): List<CoastalCity> {
        val text = context.assets.open("coastal_cities.json")
            .bufferedReader()
            .use { it.readText() }
        val array = JSONArray(text)
        return List(array.length()) { i ->
            val item = array.getJSONObject(i)
            CoastalCity(item.getString("city"), item.getString("state"))
        }
    }

    // Best matches first: starts with the query, then a word starts with it, then contains it.
    fun suggest(all: List<CoastalCity>, query: String, limit: Int = 5): List<CoastalCity> {
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) return emptyList()

        val starts = all.filter { it.name.lowercase(Locale.ROOT).startsWith(q) }
        val wordStarts = all.filter { city ->
            city !in starts &&
                city.name.lowercase(Locale.ROOT).split(' ', '-').any { it.startsWith(q) }
        }
        val contains = all.filter { city ->
            city !in starts && city !in wordStarts &&
                city.name.lowercase(Locale.ROOT).contains(q)
        }
        return (starts + wordStarts + contains).take(limit)
    }

    // Exact match on "City" or "City, State" (used when the user types and presses search).
    fun find(all: List<CoastalCity>, text: String): CoastalCity? {
        val t = text.trim()
        return all.firstOrNull {
            it.name.equals(t, ignoreCase = true) || it.label.equals(t, ignoreCase = true)
        }
    }
}
