package com.example.kane_app

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavHostController,
    walletName: String,
    eWalletType: String
) {
    var transactionAmount by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("Pemasukan") }
    var transactionDate by remember { mutableStateOf("") }
    var transactionName by remember { mutableStateOf("") }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTransactionTypeDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var walletId by remember { mutableStateOf("") }
    var walletBalance by remember { mutableStateOf(0.0) } // Balance wallet

    // Logging untuk debug nilai walletName dan walletType
    Log.d("FirestoreDebug", "Wallet Name: $walletName, Wallet Type: $eWalletType")

    // Cek wallet berdasarkan walletName atau walletType
    LaunchedEffect(walletName, eWalletType) {
        firestore.collection("users")
            .document(userEmail)
            .collection("wallets")
            .whereEqualTo("walletName", walletName) // Cek berdasarkan walletName
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // Jika walletName tidak ditemukan, coba cari berdasarkan walletType
                    firestore.collection("users")
                        .document(userEmail)
                        .collection("wallets")
                        .whereEqualTo("eWalletType", eWalletType) // Cek berdasarkan walletType
                        .get()
                        .addOnSuccessListener { typeResult ->
                            if (typeResult.documents.isNotEmpty()) {
                                walletId = typeResult.documents.first().id // Ambil walletId
                                walletBalance = typeResult.documents.first().getDouble("balance") ?: 0.0 // Ambil saldo
                            } else {
                                // Tidak ada wallet ditemukan
                                Toast.makeText(navController.context, "Wallet not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    walletId = result.documents.first().id // Ambil walletId jika ditemukan berdasarkan walletName
                    walletBalance = result.documents.first().getDouble("balance") ?: 0.0 // Ambil saldo
                }
            }
            .addOnFailureListener {
                Toast.makeText(navController.context, "Error fetching wallet", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction", style = MaterialTheme.typography.titleLarge) }, navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Input Nama Transaksi
                OutlinedTextField(
                    value = transactionName,
                    onValueChange = { transactionName = it },
                    label = { Text("Transaction Name") },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Input Jumlah Transaksi
                OutlinedTextField(
                    value = transactionAmount,
                    onValueChange = { transactionAmount = it },
                    label = { Text("Transaction Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Pilih Tanggal Transaksi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = transactionDate,
                        onValueChange = {},
                        label = { Text("Transaction Date") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showDatePickerDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar),
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Dialog Pemilih Tanggal
                if (showDatePickerDialog) {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        navController.context,
                        { _, selectedYear, selectedMonth, selectedDay ->
                            transactionDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                            showDatePickerDialog = false
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }

                // Pilih Jenis Transaksi
                Text(
                    text = "Transaction Type",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTransactionTypeDialog = true }
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(transactionType, style = MaterialTheme.typography.bodyLarge)
                        Icon(
                            painter = painterResource(R.drawable.ic_dropdown),
                            contentDescription = "Dropdown",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Dialog Pilih Jenis Transaksi
                if (showTransactionTypeDialog) {
                    AlertDialog(
                        onDismissRequest = { showTransactionTypeDialog = false },
                        title = { Text("Select Transaction Type") },
                        text = {
                            Column {
                                listOf("Pemasukan", "Pengeluaran").forEach { type ->
                                    Text(
                                        text = type,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                transactionType = type
                                                showTransactionTypeDialog = false
                                                val amount = transactionAmount.toDoubleOrNull() ?: 0.0
                                                walletBalance = if (transactionType == "Pemasukan") {
                                                    walletBalance + amount
                                                } else {
                                                    walletBalance - amount
                                                }
                                                if (walletId.isNotEmpty()) {
                                                    firestore.collection("users")
                                                        .document(userEmail)
                                                        .collection("wallets")
                                                        .document(walletId)
                                                        .update("balance", walletBalance)
                                                        .addOnSuccessListener {
                                                            // Berhasil update
                                                            Log.d(
                                                                "WalletUpdate",
                                                                "Wallet balance updated"
                                                            )
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Toast.makeText(
                                                                navController.context,
                                                                "Failed to update wallet balance",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                }
                                            }
                                            .padding(8.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {}
                    )
                }

                // Tombol Simpan Transaksi
                Button(
                    onClick = {
                        if (walletId.isNotEmpty()) {
                            // Konversi jumlah transaksi menjadi angka
                            val amount = transactionAmount.toDoubleOrNull() ?: 0.0
                            val newBalance = if (transactionType == "Pemasukan") {
                                walletBalance + amount
                            } else {
                                walletBalance - amount
                            }

                            val transaction = mapOf(
                                "name" to transactionName,
                                "type" to transactionType,
                                "amount" to amount,
                                "date" to transactionDate
                            )

                            // Menambahkan transaksi pada wallet
                            firestore.collection("users")
                                .document(userEmail)
                                .collection("wallets")
                                .document(walletId) // Menambahkan transaksi pada walletId yang sesuai
                                .collection("transactions")
                                .add(transaction)
                                .addOnSuccessListener {
                                    // Update saldo wallet
                                    firestore.collection("users")
                                        .document(userEmail)
                                        .collection("wallets")
                                        .document(walletId)
                                        .update("balance", newBalance)
                                        .addOnSuccessListener {
                                            navController.popBackStack()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(navController.context, "Failed to update balance", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    // Handle error
                                    Toast.makeText(navController.context, "Failed to add transaction", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(navController.context, "Wallet not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save Transaction")
                }
            }
        }
    }
}