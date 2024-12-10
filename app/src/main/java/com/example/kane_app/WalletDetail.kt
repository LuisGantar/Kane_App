package com.example.kane_app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletDetailScreen(navController: NavHostController, walletName: String, userEmail: String, eWalletType: String) {
    val balance = remember { mutableStateOf(0.0) }
    val transactions = remember { mutableStateListOf<Transaction>() }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }
    val wallet = remember { mutableStateOf<Wallet?>(null) }
    val eWalletType = remember { mutableStateOf("") }
    val walletId = remember { mutableStateOf("") }
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email


    LaunchedEffect(walletName) {
        val db = FirebaseFirestore.getInstance()

        // Fetch the wallet data using the userEmail and walletName
        val walletDoc = db.collection("users").document(userEmail)
            .collection("wallets")
            .whereEqualTo("walletName", walletName)
            .get()

        walletDoc.addOnSuccessListener { result ->
            if (result.isEmpty) {
                errorMessage.value = "Wallet not found"
                isLoading.value = false
                return@addOnSuccessListener
            }

            val walletData = result.documents.firstOrNull()?.toObject(Wallet::class.java)
            walletData?.let {
                wallet.value = it // Assign wallet to state
                balance.value = it.balance

                // Now fetch transactions from the correct walletId
                val transactionsRef = db.collection("users")
                    .document(userEmail)
                    .collection("wallets")
                    .document(it.walletId)
                    .collection("transactions")
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)

                transactionsRef.get()
                    .addOnSuccessListener { querySnapshot ->
                        transactions.clear()
                        var updatedBalance = it.balance
                        for (document in querySnapshot.documents) {
                            // Use safe type casting and null coalescing
                            val transactionData = document.data
                            if (transactionData != null) {
                                val transaction = Transaction(
                                    date = (transactionData["date"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                                    name = transactionData["name"] as? String ?: "",
                                    type = transactionData["type"] as? String ?: "",
                                    amount = (transactionData["amount"] as? Number)?.toDouble() ?: 0.0
                                )

                                transactions.add(transaction)
                                if (transaction.type == "Pemasukan") {
                                    updatedBalance += transaction.amount
                                } else if (transaction.type == "Pengeluaran") {
                                    updatedBalance -= transaction.amount
                                }
                            }
                        }
                        balance.value = updatedBalance
                        isLoading.value = false
                    }
                    .addOnFailureListener {
                        isLoading.value = false
                        errorMessage.value = "Failed to load transactions. Please try again."
                    }
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Back Button to Home
        IconButton(onClick = { navController.navigate("home") }) {
            Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Back to Home")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance Display
        Text(
            text = "Balance",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "IDR ${balance.value}", // Display the correct balance value
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Add Transaction Button
        wallet.value?.let { walletData ->
            Button(
                onClick = {
                    // Navigate only if wallet is not null
                    navController.navigate("add_transaction/$walletName/$eWalletType") // Navigate to AddTransactionScreen
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Add Transaction")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Transaction Details
        Text(text = "Transaction Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (transactions.isEmpty()) {
            Text("No transactions added.")
        } else {
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionCard(transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Name: ${transaction.name}")
            Text("Type: ${transaction.type}")
            Text("Amount: IDR ${transaction.amount}")
            Text("Date: ${transaction.date}")
        }
    }
}

data class Transaction(
    var date: Date = Date(),
    val name: String = "",
    val type: String = "",
    val amount: Double = 0.0
)