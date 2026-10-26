package com.example.mynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.mynotes.ui.theme.MyNotesTheme
import com.example.mynotes.uii.LoginScreen
import com.example.mynotes.uii.NotesScreen
import com.example.mynotes.uii.RegisterScreen
import com.google.firebase.auth.FirebaseAuth

enum class AppScreen {
    LOGIN,
    REGISTER,
    NOTES
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyNotesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MyNotesApp()
                }
            }
        }
    }
}

@Composable
fun MyNotesApp() {
    val auth = FirebaseAuth.getInstance()

    var currentScreen by remember {
        mutableStateOf(
            if (auth.currentUser != null) AppScreen.NOTES else AppScreen.LOGIN
        )
    }

    // Écoute de l'état d'authentification Firebase
    DisposableEffect(auth) {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                currentScreen = AppScreen.NOTES
            } else if (currentScreen == AppScreen.NOTES) {
                currentScreen = AppScreen.LOGIN
            }
        }

        auth.addAuthStateListener(authStateListener)
        onDispose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    Crossfade(
        targetState = currentScreen,
        label = "AppScreenTransition"
    ) { screen ->
        when (screen) {
            AppScreen.NOTES -> {
                NotesScreen(
                    onLogout = {
                        auth.signOut()
                        currentScreen = AppScreen.LOGIN
                    }
                )
            }

            AppScreen.REGISTER -> {
                RegisterScreen(
                    onRegisterSuccess = {
                        currentScreen = AppScreen.NOTES
                    },
                    onBackToLogin = {
                        currentScreen = AppScreen.LOGIN
                    }
                )
            }

            AppScreen.LOGIN -> {
                LoginScreen(
                    onLoginSuccess = {
                        currentScreen = AppScreen.NOTES
                    },
                    onCreateAccount = {
                        currentScreen = AppScreen.REGISTER
                    }
                )
            }
        }
    }
}