package com.radioisaac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.radioisaac.ui.RadioScreen
import com.radioisaac.ui.theme.RadioIsaacTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RadioIsaacTheme {
                RadioScreen()
            }
        }
    }
}
