package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Account (Рахунки)
@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val kind: String,          // cash, current, savings, deposit, debt, income_source, expense_category
    val balanceNature: String,  // asset, liability
    val currency: String,       // UAH, USD, EUR, PLN
    val currentBalance: Double, 
    val initialBalance: Double,
    val depositRate: Double? = null,
    val depositTermMonths: Int? = null,
    val depositStartDate: Long? = null,
    val depositEndDate: Long? = null,
    val creditLimitMinor: Int? = null,
    val isArchived: Boolean = false,
    val displayInTotalBalance: Boolean = true
)

// Transaction (Операції)
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,                   // income, expense, transfer, debt_given, debt_received, debt_repayment_in, debt_repayment_out, adjustment
    val status: String = "confirmed",   // confirmed, needs_review, reconciled, reversed
    val kind: String = "regular",       // regular, refund, reversal, cashback, fee, fx_adjustment, opening_balance, balance_correction
    val accountId: Int,                 // З якого рахунку
    val amount: Double,                 // Сума
    val currency: String,               // Валюта рахунку-джерела
    val counterpartyAccountId: Int? = null, // Отримувач (для transfer / debt)
    val counterpartyAmount: Double? = null, // Сума в валюті отримувача
    val counterpartyCurrency: String? = null,
    val category: String,               // Категорія
    val description: String,            // Опис / коментар
    val date: Long = System.currentTimeMillis(),
    val visibility: String = "shared",  // shared, private, hidden_details
    val tags: String = ""               // Added for sub-category / tags feature
)

// Budget (Бюджети)
@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val limitAmount: Double,
    val currency: String = "UAH"
)

// Counterparty (Контрагенти)
@Entity(tableName = "counterparties")
data class Counterparty(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val notes: String = ""
)

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY id ASC")
    fun getAllActiveAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Int): Account?

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Update
    suspend fun updateAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR counterpartyAccountId = :accountId ORDER BY date DESC")
    fun getTransactionsByAccount(accountId: Int): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE category = :category LIMIT 1")
    suspend fun getBudgetByCategory(category: String): Budget?
}

@Dao
interface CounterpartyDao {
    @Query("SELECT * FROM counterparties")
    fun getAllCounterparties(): Flow<List<Counterparty>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounterparty(counterparty: Counterparty)

    @Delete
    suspend fun deleteCounterparty(counterparty: Counterparty)
}

@Database(
    entities = [Account::class, Transaction::class, Budget::class, Counterparty::class],
    version = 4,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun counterpartyDao(): CounterpartyDao
}

class FinanceRepository(private val db: FinanceDatabase) {
    val allAccounts: Flow<List<Account>> = db.accountDao().getAllActiveAccounts()
    val allTransactions: Flow<List<Transaction>> = db.transactionDao().getAllTransactions()
    val allBudgets: Flow<List<Budget>> = db.budgetDao().getAllBudgets()
    val allCounterparties: Flow<List<Counterparty>> = db.counterpartyDao().getAllCounterparties()

    suspend fun insertAccount(account: Account) {
        db.accountDao().insertAccount(account)
    }

    suspend fun updateAccount(account: Account) {
        db.accountDao().updateAccount(account)
    }

    suspend fun deleteAccount(account: Account) {
        db.accountDao().deleteAccount(account)
    }

    suspend fun getAccountById(id: Int): Account? {
        return db.accountDao().getAccountById(id)
    }

    suspend fun getAccountsCount(): Int {
        return db.accountDao().getAccountsCount()
    }

    suspend fun insertTransaction(transaction: Transaction) {
        // Automatically recalculate account balances on background
        db.transactionDao().insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        db.transactionDao().deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) {
        db.transactionDao().deleteTransactionById(id)
    }

    suspend fun insertBudget(budget: Budget) {
        db.budgetDao().insertBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        db.budgetDao().deleteBudget(budget)
    }

    suspend fun insertCounterparty(counterparty: Counterparty) {
        db.counterpartyDao().insertCounterparty(counterparty)
    }

    suspend fun deleteCounterparty(counterparty: Counterparty) {
        db.counterpartyDao().deleteCounterparty(counterparty)
    }
}
