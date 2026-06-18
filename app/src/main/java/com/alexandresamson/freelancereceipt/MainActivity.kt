package com.alexandresamson.freelancereceipt

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.alexandresamson.freelancereceipt.navigation.AppNavigation
import com.alexandresamson.freelancereceipt.ui.theme.FreelanceReceiptTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // MUST be called before super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            FreelanceReceiptTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
