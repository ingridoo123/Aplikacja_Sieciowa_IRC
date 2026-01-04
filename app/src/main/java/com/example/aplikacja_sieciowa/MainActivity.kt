package com.example.aplikacja_sieciowa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.aplikacja_sieciowa.presentation.connect.ConnectScreen
import com.example.aplikacja_sieciowa.presentation.navigation.NavGraph
import com.example.aplikacja_sieciowa.ui.theme.Aplikacja_SieciowaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavGraph()
        }
    }
}