package syscall.zixuan.com.flutter_sys_call.video.takevideo.utils


import android.util.Log

import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer


object LogUtils {

    private val IS_DEBUG = true

    private val TAG = "you"

    fun e(tag: String, msg: String) {
        if (IS_DEBUG) Log.e(tag, msg)
    }

    fun i(tag: String, msg: String) {
        if (IS_DEBUG) Log.i(tag, msg)
    }

    fun d(tag: String, msg: String) {
        if (IS_DEBUG) Log.d(tag, msg)
    }

    fun v(tag: String, msg: String) {
        if (IS_DEBUG) Log.v(tag, msg)
    }

    fun w(tag: String, msg: String) {
        if (IS_DEBUG) Log.w(tag, msg)
    }

    fun e(msg: String) {
        if (IS_DEBUG) Log.e(TAG, msg)
    }

    fun i(msg: String) {
        i(TAG, msg)
    }

    fun d(msg: String) {
        d(TAG, msg)
    }

    fun v(msg: String) {
        v(TAG, msg)
    }

    fun w(msg: String) {
        w(TAG, msg)
    }

    fun i(tag: String, e: Throwable) {
        if (IS_DEBUG) {
            val info = StringWriter()
            val printWriter = PrintWriter(info)
            e.printStackTrace(printWriter)

            var cause: Throwable? = e.cause
            while (cause != null) {
                cause.printStackTrace(printWriter)
                cause = cause.cause
            }
            i(tag, info.toString())
        }
    }

    fun i(e: Throwable) {
        i(TAG, e)
    }

}
