package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.PinRepository
import com.example.ui.PinState
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.screens.PinScreen
import com.example.ui.screens.WebViewScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val repository = PinRepository(applicationContext)
    val viewModelFactory = MainViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

    setContent {
      MyApplicationTheme(darkTheme = true) {
        val navController = rememberNavController()
        val pinState = viewModel.pinState.collectAsStateWithLifecycle().value

        NavHost(
            navController = navController,
            startDestination = "pin_check"
        ) {
            composable("pin_check") {
                when (pinState) {
                    is PinState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is PinState.Setup -> {
                        PinScreen(
                            isSetup = true,
                            expectedPin = null,
                            onPinSuccess = { newPin ->
                                viewModel.savePin(newPin)
                                navController.navigate("webview") {
                                    popUpTo("pin_check") { inclusive = true }
                                }
                            }
                        )
                    }
                    is PinState.Exists -> {
                        PinScreen(
                            isSetup = false,
                            expectedPin = pinState.pin,
                            onPinSuccess = {
                                navController.navigate("webview") {
                                    popUpTo("pin_check") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }

            composable("webview") {
                // Enter immersive mode
                val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

                WebViewScreen(url = "https://www.instagram.com/")
            }
        }
      }
    }
  }

  // Restore system bars when leaving
  override fun onDestroy() {
    super.onDestroy()
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
  }
}
