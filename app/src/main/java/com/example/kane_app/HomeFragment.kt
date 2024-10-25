package com.example.kane_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

class HomeFragment : Fragment() {

    @Composable
    fun HomeScreen(navController: NavHostController) {
        Scaffold(
            topBar = { TopAppBarWithIcons() }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    @Composable
    fun HomeContent() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_default_profile),
                    contentDescription = "Profile Icon",
                    modifier = Modifier.size(64.dp)
                )

                IconButton(
                    onClick = {},
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_notification),
                        contentDescription = "Notification Icon"
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBarWithIcons() {
        Column(
            modifier = Modifier.padding(top = 46.dp)
        ) {
            TopAppBar(
                title = {},
                navigationIcon = {

                    Card(
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .padding(start = 40.dp, top = 8.dp, bottom = 8.dp)
                            .size(48.dp) // Ukuran total Card
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_default_profile),
                            contentDescription = "Profile Icon",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    }
                },
                actions = {

                    Box(
                        modifier = Modifier
                            .padding(end = 40.dp, top = 8.dp, bottom = 8.dp)
                            .size(48.dp)
                            .border(1.dp, Color.Black, shape = MaterialTheme.shapes.small)
                            .padding(4.dp)
                    ) {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.ic_notification),
                                contentDescription = "Notification Icon",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    }

}