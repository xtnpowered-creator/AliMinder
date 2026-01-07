package com.aliminder.app.presentation.util

import android.util.Log

class GlobalCrashHandler : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Log.e("GlobalCrashHandler", "CRASH DETECTED: ${throwable.message}", throwable)
        defaultHandler?.uncaughtException(thread, throwable)
    }
}
