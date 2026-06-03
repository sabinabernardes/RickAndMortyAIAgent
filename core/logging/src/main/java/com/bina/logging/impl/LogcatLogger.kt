package com.bina.logging.impl

import android.util.Log
import com.bina.logging.AppLogger

class LogcatLogger : AppLogger {
    override fun debug(tag: String, message: String) = Log.d(tag, message).let {}
    override fun info(tag: String, message: String) = Log.i(tag, message).let {}
    override fun warn(tag: String, message: String, throwable: Throwable?) =
        Log.w(tag, message, throwable).let {}
    override fun error(tag: String, message: String, throwable: Throwable?) =
        Log.e(tag, message, throwable).let {}
}
