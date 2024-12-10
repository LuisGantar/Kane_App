package com.example.kane_app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBudgetScreen(
    navController: NavHostController,
    budgets: MutableList<Budget>,
    userEmail: String // Email pengguna untuk mengambil data wallet
) {
    var name by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var wallet by remember { mutableStateOf("") }
    var walletId by remember { mutableStateOf("") }

    var periodExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var walletExpanded by remember { mutableStateOf(false) }

    val periodOptions = listOf("Daily", "Weekly", "Monthly", "Yearly")
    val categoryOptions = listOf("Food & Beverages", "Transport", "Shopping", "Entertainment", "Others")
    val walletList = remember { mutableStateListOf<Wallet>() }

    // Ambil data wallet dari Firestore
    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        try {
            val snapshot = firestore
                .collection("users")
                .document(userEmail)
                .collection("wallets")
                .get()
                .await()

            // Parsing dokumen menjadi daftar Wallet
            val wallets = snapshot.documents.map { doc ->
                Wallet(
                    walletId = doc.id,
                    walletName = doc.getString("walletName") ?: "Unnamed Wallet",
                    eWalletType = doc.getString("eWalletType") ?: "Unknown",
                    balance = doc.getDouble("balance") ?: 0.0,
                    initialBalance = doc.getString("initialBalance") ?: "0.0"
                )
            }
            walletList.clear()
            walletList.addAll(wallets)
        } catch (e: Exception) {
            e.printStackTrace() // Log error jika terjadi
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Budget") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Input untuk nama budget
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Budget Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown untuk period
            DropdownTextField(
                value = period,
                label = "Period",
                options = periodOptions,
                onValueChange = { period = it },
                expanded = periodExpanded,
                onExpandedChange = { periodExpanded = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown untuk category
            DropdownTextField(
                value = category,
                label = "Category",
                options = categoryOptions,
                onValueChange = { category = it },
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Input untuk jumlah budget
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown untuk wallet
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = wallet,
                    onValueChange = { /* Tidak perlu diubah manual */ },
                    label = { Text("Wallet") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { walletExpanded = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dropdown),
                                contentDescription = "Dropdown Icon"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = walletExpanded,
                    onDismissRequest = { walletExpanded = false }
                ) {
                    walletList.forEach { walletItem ->
                        DropdownMenuItem(
                            onClick = {
                                wallet = walletItem.walletName // Simpan nama wallet
                                walletId = walletItem.walletId // Simpan walletId
                                walletExpanded = false // Tutup dropdown
                            },
                            text = {
                                Column {
                                    Text(walletItem.walletName) // Nama wallet
                                    Text(
                                        "Balance: ${walletItem.balance}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.isNotBlank() && period.isNotBlank() && amount.isNotBlank() && category.isNotBlank() && wallet.isNotBlank()) {
                        // Menyimpan budget ke daftar local
                        val budget = Budget(
                            name = name,
                            period = period,
                            amount = amount.toDouble(),
                            category = category,
                            wallet = wallet,
                            balance = amount.toDouble()
                        )
                        budgets.add(budget)

                        // Menyimpan budget ke Firestore
                        val firestore = FirebaseFirestore.getInstance()
                        val userDocument = firestore.collection("users").document(userEmail)
                        val walletDocument = userDocument.collection("wallets").document(walletId)

                        walletDocument.collection("budgets").add(budget)
                            .addOnSuccessListener { documentReference ->
                                // Budget berhasil disimpan
                                println("Budget successfully saved with ID: ${documentReference.id}")
                                navController.popBackStack() // Kembali ke halaman sebelumnya setelah menyimpan
                            }
                            .addOnFailureListener { exception ->
                                // Menangani kesalahan jika penyimpanan gagal
                                println("Error saving budget: $exception")
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Budget")
            }
        }
    }
}
@Composable
fun DropdownTextField(
    value: String,
    label: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit

) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { onExpandedChange(!expanded) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dropdown), // Pastikan ikon dropdown tersedia
                        contentDescription = "Dropdown Icon"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onValueChange(option) // Pilihan dipilih
                        onExpandedChange(false) // Tutup dropdown
                    },
                    text = { Text(option) }
                )
            }
        }
    }
}