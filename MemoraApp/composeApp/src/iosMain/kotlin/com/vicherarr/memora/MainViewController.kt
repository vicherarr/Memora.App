package com.vicherarr.memora

import androidx.compose.ui.window.ComposeUIViewController
import com.vicherarr.memora.di.appModule
import com.vicherarr.memora.di.databaseModule
import com.vicherarr.memora.di.iosDatabaseModule
import com.vicherarr.memora.di.platformModule
import org.koin.compose.KoinApplication

fun MainViewController() = ComposeUIViewController { 
    KoinApplication(
        application = {
            modules(
                appModule,
                databaseModule,
                iosDatabaseModule,
                platformModule()
            )
        }
    ) {
        App()
    }
}