package com.example.kane_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kane_app.ui.InputEmailFragment
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appCheck = FirebaseAppCheck.getInstance()
        appCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") { OnBoarding().OnboardingScreen(navController) }
        composable("input_email") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            InputEmailFragment().InputEmailScreen(navController, firebaseAuth, firestore)
        }
        composable("login_with_email/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            LoginFragment().LoginWithEmailScreen(navController, firebaseAuth, email)
        }
        composable("register/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            RegisterFragment().RegisterScreen(navController, email, firebaseAuth, firestore)
        }
        composable("enter_name/{email}/{password}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""
            NameFragment().NameScreen(navController, email, password, firebaseAuth, firestore)
        }
        composable("pin_login/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            PinFragment().PinScreen(navController, email, null, null, firebaseAuth, firestore, isLogin = true)
        }
        composable("pin_register/{email}/{password}/{name}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            PinFragment().PinScreen(navController, email, password, name, firebaseAuth, firestore, isLogin = false)
        }
        composable("home") { HomeFragment().HomeScreen() }
    }
}