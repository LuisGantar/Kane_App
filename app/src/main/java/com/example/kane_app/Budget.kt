package com.example.kane_app

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Budget(
    val name: String,
    val period: String,
    val amount: Double,
    val category: String,
    val wallet: String,
    val balance: Double
)

@Composable
fun BudgetingScreen(navController: NavHostController, userEmail: String) {
    val budgets = remember { mutableStateListOf<Budget>() }
    val isLoading = remember { mutableStateOf(true) }

    // Ambil data budget dari Firestore
    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        try {
            val snapshot = firestore
                .collection("users")
                .document(userEmail)
                .collection("wallets")
                .get()
                .await()

            snapshot.documents.forEach { walletDoc ->
                val walletId = walletDoc.id
                val walletBalance = walletDoc.getDouble("balance") ?: 0.0

                val budgetSnapshot = firestore
                    .collection("users")
                    .document(userEmail)
                    .collection("wallets")
                    .document(walletId)
                    .collection("budgets")
                    .get()
                    .await()

                val walletBudgets = budgetSnapshot.documents.map { doc ->
                    Budget(
                        name = doc.getString("name") ?: "",
                        period = doc.getString("period") ?: "",
                        amount = doc.getDouble("amount") ?: 0.0,
                        category = doc.getString("category") ?: "",
                        wallet = doc.getString("wallet") ?: "",
                        balance = walletBalance
                    )
                }

                budgets.addAll(walletBudgets)
            }
            isLoading.value = false
        } catch (e: Exception) {
            e.printStackTrace()
            isLoading.value = false
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Budgeting",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                budgets.forEach { budget ->
                    BudgetCard(
                        budget = budget,
                        onAmountUpdated = { updatedBudget, newAmount ->
                            // Update amount di Firestore
                            updateBudgetAmount(updatedBudget, newAmount, userEmail)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                EmptyBudgetCard(onCreateBudgetClick = {
                    navController.navigate("create_budget")
                })
            }
        }
    }
}

fun updateBudgetAmount(budget: Budget, newAmount: Double, userEmail: String) {
    val firestore = FirebaseFirestore.getInstance()
    val budgetRef = firestore
        .collection("users")
        .document(userEmail)
        .collection("wallets")
        .document(budget.wallet)
        .collection("budgets")
        .document(budget.name)

    budgetRef.update("amount", newAmount).addOnSuccessListener {
        // Tampilkan pesan sukses jika diperlukan
    }.addOnFailureListener {
        // Tampilkan pesan error jika diperlukan
    }
}

@Composable
fun BudgetCard(budget: Budget, onAmountUpdated: (Budget, Double) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var newAmount by remember { mutableStateOf(budget.amount.toString()) }

    // Hitung persentase dari amount terhadap wallet balance
    val progress = if (budget.balance > 0) {
        (budget.amount / budget.balance).toDouble().coerceIn(0.0, 1.0) // Pastikan hasilnya adalah Double
    } else {
        0.0
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click to view/edit budget */ },
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF3E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = budget.name, // Gunakan parameter budget
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Period: ${budget.period}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Amount: IDR ${"%,.0f".format(budget.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF4CAF50)
            )
            Text(
                text = "Category: ${budget.category}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Wallet: ${budget.wallet}",
                fontSize = 14.sp,
                color = Color.Gray
            )

            // Progress bar untuk memantau persentase
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = {
                    progress.toFloat()
                },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.LightGray,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Progress: ${"%.0f".format(progress * 100)}%",  // Menampilkan persentase
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
@Composable
fun EmptyBudgetCard(onCreateBudgetClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Budgets",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start with Budgets to have an efficient overview of your spending limits",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "CREATE BUDGET",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onCreateBudgetClick() } // Navigasi ke halaman create budget
            )
        }
    }
}
