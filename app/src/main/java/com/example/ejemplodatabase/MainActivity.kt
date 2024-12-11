package com.example.ejemplodatabase
//add
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.ejemplodatabase.data.NombreViewModel
import com.example.ejemplodatabase.screens.LoginScreen
import com.example.ejemplodatabase.screens.RegisterScreen
import com.example.ejemplodatabase.screens.MapScreen
import com.example.ejemplodatabase.ui.theme.EjemploDataBaseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel: NombreViewModel by viewModels()

        setContent {
            EjemploDataBaseTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: NombreViewModel) {
    // Usar remember para manejar el estado de la pantalla actual
    var currentScreen by remember { mutableStateOf("login") }

    when (currentScreen) {
        "login" -> LoginScreen(
            onNavigateToRegister = { currentScreen = "register" },
            onLoginSuccess = { currentScreen = "map" }
        )
        "register" -> RegisterScreen(
            onRegisterSuccess = { currentScreen = "login" }
        )
        "map" -> MapScreen()
    }
}
