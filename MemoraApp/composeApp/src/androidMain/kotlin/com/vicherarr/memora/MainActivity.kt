package com.vicherarr.memora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.vicherarr.memora.platform.ActivityRegistry

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Register this activity for camera lifecycle
        ActivityRegistry.setCurrentActivity(this)

        setContent {
            App()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        ActivityRegistry.clearCurrentActivity()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}