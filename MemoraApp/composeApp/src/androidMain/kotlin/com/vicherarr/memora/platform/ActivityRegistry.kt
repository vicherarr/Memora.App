package com.vicherarr.memora.platform

import androidx.activity.ComponentActivity
import java.lang.ref.WeakReference

/**
 * Registry to keep track of the current active Activity
 * Uses WeakReference to avoid memory leaks
 */
object ActivityRegistry {
    private var currentActivity: WeakReference<ComponentActivity>? = null
    
    fun setCurrentActivity(activity: ComponentActivity) {
        currentActivity = WeakReference(activity)
    }
    
    fun getCurrentActivity(): ComponentActivity? {
        return currentActivity?.get()
    }
    
    fun clearCurrentActivity() {
        currentActivity?.clear()
        currentActivity = null
    }
}