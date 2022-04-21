package mx.ipn.upiiz.darcazaa.data.models

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.reflect.KClass

class UserPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    private val _preferenceChanges = MutableStateFlow<Pair<PreferenceKeys<*>, *>>(Pair(PreferenceKeys.Url, ""))
    val preferenceChanges = _preferenceChanges.asSharedFlow()

    @Suppress("UNCHECKED_CAST")
    fun <T: Any>set(preferenceKey: PreferenceKeys<T>, value: T?){
        if(value == null) return remove(preferenceKey)

        val applied = when(value){
            is Int -> preferences.edit().putInt(preferenceKey.key, value)
            is Float -> preferences.edit().putFloat(preferenceKey.key, value)
            is Boolean -> preferences.edit().putBoolean(preferenceKey.key, value)
            is Long -> preferences.edit().putLong(preferenceKey.key, value)
            is String -> preferences.edit().putString(preferenceKey.key, value)
            else -> null
        }?.apply()

        if(applied != null){
            _preferenceChanges.tryEmit(Pair(preferenceKey, value))
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any>get(preferenceKey: PreferenceKeys<T>, default: T? = null): T? = if(preferences.contains(preferenceKey.key)){
        when(preferenceKey.clazz){
            Int::class -> preferences.getInt(preferenceKey.key, 0) as T
            Float::class -> preferences.getFloat(preferenceKey.key, 0f) as T
            Boolean::class -> preferences.getBoolean(preferenceKey.key, false) as T
            Long::class -> preferences.getLong(preferenceKey.key, 0) as T
            String::class -> preferences.getString(preferenceKey.key, "") as T
            else -> default
        }
    } else default

    fun <T: Any>remove(preferenceKey: PreferenceKeys<T>){
        preferences.edit().remove(preferenceKey.key).apply()
        _preferenceChanges.tryEmit(Pair(preferenceKey, null))
    }
}

sealed class PreferenceKeys<T: Any>(val key: String, val clazz: KClass<T>) {
    object Url: PreferenceKeys<String>("url", String::class)
}