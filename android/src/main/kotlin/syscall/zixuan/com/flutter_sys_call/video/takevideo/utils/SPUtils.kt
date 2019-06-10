package syscall.zixuan.com.flutter_sys_call.video.takevideo.utils


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.SharedPreferencesCompat


object SPUtils {

    /**
     * 保存在手机里面的文件名
     */
    val FILE_NAME = "share_data"
    /**
     * 是否引导
     */
    val HAS_SPLASH = "has_splash"
    /**
     * 用户Session值
     */
    val USER_ACCOUNT = "user_account"

    /**
     * 保存数据
     * @param context
     * @param key
     * @param value
     */
    fun put(context: Context, key: String, value: Any) {
        val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        if (value is String) {
            editor.putString(key, value)
        } else if (value is Int) {
            editor.putInt(key, value)
        } else if (value is Boolean) {
            editor.putBoolean(key, value)
        } else if (value is Float) {
            editor.putFloat(key, value)
        } else if (value is Long) {
            editor.putLong(key, value)
        } else {
            editor.putString(key, value.toString())
        }

        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor)
    }

    /**
     * 获取数据
     * @param context
     * @param key
     * @param defValue
     * @return
     */
    operator fun get(context: Context, key: String, defValue: Any): Any? {
        val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        if (defValue is String) {
            return sp.getString(key, defValue)
        } else if (defValue is Int) {
            return sp.getInt(key, defValue)
        } else if (defValue is Boolean) {
            return sp.getBoolean(key, defValue)
        } else if (defValue is Float) {
            return sp.getFloat(key, defValue)
        } else if (defValue is Long) {
            return sp.getLong(key, defValue)
        }
        return null
    }

    /**
     * 移除数据
     * @param context
     * @param key
     */
    fun remove(context: Context, key: String) {
        val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.remove(key)
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor)
    }

    /**
     * 清空保存数据
     * @param context
     */
    fun clear(context: Context) {
        val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.clear()
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor)
    }

}
