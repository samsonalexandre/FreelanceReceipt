package com.alexandresamson.freelancereceipt

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.alexandresamson.freelancereceipt.navigation.AppNavigation
import com.alexandresamson.freelancereceipt.ui.theme.FreelanceReceiptTheme

// WICHTIG: Erbt von FragmentActivity (für die Biometrie!)
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreelanceReceiptTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // HIER ist die Magie: Wir rufen unsere Navigation auf!
                    AppNavigation()
                }
            }
        }
    }
}