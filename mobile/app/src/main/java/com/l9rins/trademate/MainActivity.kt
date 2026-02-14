package com.l9rins.trademate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.l9rins.trademate.data.SessionManager
import com.l9rins.trademate.ui.auth.LoginScreen
import com.l9rins.trademate.ui.auth.RegisterScreen
import com.l9rins.trademate.ui.theme.TextSecondary
import com.l9rins.trademate.ui.theme.TradeMateGreen
import com.l9rins.trademate.ui.theme.TradeMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: MainViewModel by viewModels()

        setContent {
            TradeMateTheme {
                val context = LocalContext.current
                val sessionManager = remember { SessionManager(context) }

                // STATE MANAGEMENT FOR NAVIGATION
                // 0 = Login, 1 = Register, 2 = Dashboard (Protected)
                var currentScreen by remember { mutableStateOf(if (sessionManager.getToken() != null) 2 else 0) }

                when (currentScreen) {
                    0 -> LoginScreen(
                        onLoginSuccess = { currentScreen = 2 },
                        onNavigateToRegister = { currentScreen = 1 }
                    )
                    1 -> RegisterScreen(
                        onRegisterSuccess = { currentScreen = 0 },
                        onNavigateToLogin = { currentScreen = 0 }
                    )
                    2 -> MainAppContent(
                        viewModel = viewModel,
                        onLogout = {
                            sessionManager.logout()
                            currentScreen = 0
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel, onLogout: () -> Unit) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(containerColor = Color.White, contentColor = TextSecondary) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = selectedIndex == 0,
                    onClick = { selectedIndex = 0 },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = TradeMateGreen)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.People, "Clients") },
                    label = { Text("Clients") },
                    selected = selectedIndex == 1,
                    onClick = { selectedIndex = 1 },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = TradeMateGreen)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Work, "Jobs") },
                    label = { Text("Jobs") },
                    selected = selectedIndex == 2,
                    onClick = { selectedIndex = 2 },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = TradeMateGreen)
                )
                // LOGOUT BUTTON
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ExitToApp, "Logout", tint = Color.Red) },
                    label = { Text("Logout", color = Color.Red) },
                    selected = false,
                    onClick = onLogout
                )
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedIndex) {
                0 -> DashboardScreen(viewModel = viewModel)
                1 -> DirectoryScreen(
                    titleMain = "CLIENT",
                    titleHighlight = "DIRECTORY",
                    subtitle = "MANAGE YOUR CUSTOMERS",
                    buttonText = "ADD CLIENT",
                    viewModel = viewModel
                )
                2 -> JobScreen(viewModel = viewModel)
            }
        }
    }
}