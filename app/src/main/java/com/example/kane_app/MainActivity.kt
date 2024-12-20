package com.example.kane_app

import CreateWalletScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kane_app.ui.InputEmailFragment
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavType
import androidx.navigation.navArgument


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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val budgets = remember { mutableStateListOf<Budget>() }
    val userEmail = firebaseAuth.currentUser?.email ?: ""

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf("home", "budgeting", "insights", "settings", "add")) {
                BottomNavigationBar(navController)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = "onboarding"
            ) {
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
                    PinFragment().PinScreen(
                        navController,
                        email,
                        null,
                        null,
                        firebaseAuth,
                        firestore,
                        isLogin = true
                    )
                }
                composable("pin_register/{email}/{password}/{name}") { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    val password = backStackEntry.arguments?.getString("password") ?: ""
                    val name = backStackEntry.arguments?.getString("name") ?: ""
                    PinFragment().PinScreen(
                        navController,
                        email,
                        password,
                        name,
                        firebaseAuth,
                        firestore,
                        isLogin = false
                    )
                }
                // Pass the userEmail parameter when navigating to the create_wallet route
                composable("create_wallet") { backStackEntry ->
                    CreateWalletScreen(
                        navController = navController,
                        onBackClick = { navController.popBackStack() },
                        onConfirmClick = { navController.navigate("home")
                        }
                    )
                }

                composable("home") { HomeFragment().HomeScreen(navController) }
                composable("budgeting") { BudgetingScreen(navController, userEmail = userEmail) }
                composable("add") { AddScreen(navController) }
                composable("insights") { InsightsScreen(navController) }
                composable("settings") { SettingsScreen(navController) }

                // In your NavHost setup
                composable("walletDetails/{userEmail}/{walletName}/{eWalletType}") { backStackEntry ->
                    val userEmail = backStackEntry.arguments?.getString("userEmail") ?: ""
                    val walletName = backStackEntry.arguments?.getString("walletName") ?: ""
                    val eWalletType = backStackEntry.arguments?.getString("eWalletType") ?: ""

                    WalletDetailScreen(navController = navController, walletName, userEmail = userEmail, eWalletType)
                    // Lakukan query dan pengolahan data seperti sebelumnya
                    val db = FirebaseFirestore.getInstance()
                    var wallet by remember { mutableStateOf<Wallet?>(null) }

                    // Tampilkan WalletDetailScreen saat wallet sudah dimuat
                    if (wallet != null) {
                        WalletDetailScreen(navController, walletName, userEmail, eWalletType)
                    } else {
                        // Tampilkan loading spinner atau pesan

                    }
                }
                composable(
                    "add_transaction/{walletName}/{eWalletType}",
                    arguments = listOf(
                        navArgument("walletName") { type = NavType.StringType },
                        navArgument("eWalletType") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val walletName = backStackEntry.arguments?.getString("walletName") ?: ""
                    val eWalletType = backStackEntry.arguments?.getString("eWalletType") ?: ""
                    AddTransactionScreen(navController, walletName, eWalletType)
                }
                composable("create_budget") {
                    CreateBudgetScreen(navController = navController, budgets = budgets, userEmail = firebaseAuth.currentUser?.email ?: "")
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box {
            // Background Navigation Bar
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                NavigationBarItem(
                    icon = { NavIcon(R.drawable.ic_home, "Home") },
                    label = { Text("Home") },
                    selected = navController.currentBackStackEntry?.destination?.route == "home",
                    onClick = { navController.navigate("home") }
                )
                NavigationBarItem(
                    icon = { NavIcon(R.drawable.ic_budget, "Budgeting") },
                    label = { Text("Budgeting") },
                    selected = navController.currentBackStackEntry?.destination?.route == "budgeting",
                    onClick = { navController.navigate("budgeting") }
                )
                Spacer(modifier = Modifier.width(48.dp))
                NavigationBarItem(
                    icon = { NavIcon(R.drawable.ic_insights, "Insights") },
                    label = { Text("Insights") },
                    selected = navController.currentBackStackEntry?.destination?.route == "insights",
                    onClick = { navController.navigate("insights") }
                )
                NavigationBarItem(
                    icon = { NavIcon(R.drawable.ic_setting, "Settings") },
                    label = { Text("Settings") },
                    selected = navController.currentBackStackEntry?.destination?.route == "settings",
                    onClick = { navController.navigate("settings") }
                )
            }

            //Bagian Add untuk Float
            FloatingActionButton(
                onClick = { navController.navigate("add") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-10).dp)
                    .size(56.dp),
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = "Add",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun NavIcon(iconResId: Int, contentDescription: String) {
    Icon(
        painter = painterResource(id = iconResId),
        contentDescription = contentDescription,
        modifier = Modifier.size(24.dp)
    )
}