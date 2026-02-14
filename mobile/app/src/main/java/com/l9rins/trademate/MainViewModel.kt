package com.l9rins.trademate

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.l9rins.trademate.data.AppDatabase
import com.l9rins.trademate.data.Client
import com.l9rins.trademate.data.ClientRepository
import com.l9rins.trademate.data.Job
import com.l9rins.trademate.data.JobDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClientRepository
    private val jobDao: JobDao

    // CLIENTS
    private val _allClients = MutableStateFlow<List<Client>>(emptyList())
    val clientList: StateFlow<List<Client>> get() = _allClients

    val searchQuery = MutableStateFlow("")
    val filteredClients: StateFlow<List<Client>>

    // JOBS
    val allJobs: StateFlow<List<Job>>

    // STATS
    val statsActive: StateFlow<Int>
    val statsPending: StateFlow<Int>
    val statsCompleted: StateFlow<Int>
    val statsRevenue: StateFlow<Double>

    init {
        val db = AppDatabase.getDatabase(application)
        val clientDao = db.clientDao()
        jobDao = db.jobDao()
        repository = ClientRepository(clientDao)

        viewModelScope.launch {
            repository.allClients.collect { _allClients.value = it }
        }

        filteredClients = combine(_allClients, searchQuery) { clients, query ->
            if (query.isBlank()) clients
            else clients.filter { it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true) }
        }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

        allJobs = jobDao.getAllJobs().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        statsActive = jobDao.getActiveCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        statsPending = jobDao.getPendingCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        statsCompleted = jobDao.getCompletedCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        statsRevenue = jobDao.getTotalRevenue().map { it ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    }

    // ACTIONS
    fun addClient(name: String, email: String, phone: String) {
        viewModelScope.launch { repository.insert(Client(name = name, email = email, phone = phone)) }
    }
    fun deleteClient(client: Client) {
        viewModelScope.launch { repository.delete(client) }
    }
    fun makeCall(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(intent)
    }

    fun addJob(title: String, clientName: String, price: Double, status: String) {
        viewModelScope.launch {
            jobDao.insertJob(Job(title = title, clientName = clientName, price = price, status = status, date = "Today"))
        }
    }
    fun deleteJob(job: Job) {
        viewModelScope.launch { jobDao.deleteJob(job) }
    }

    // NEW: Update Job (For Photo)
    fun updateJob(job: Job) {
        viewModelScope.launch {
            jobDao.insertJob(job)
        }
    }

    // NEW: Share Invoice
    fun shareInvoice(context: android.content.Context, job: Job) {
        PDFGenerator.generateAndShareInvoice(context, job)
    }

    // NEW: Sync Calendar
    fun addToCalendar(context: android.content.Context, job: Job) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = android.provider.CalendarContract.Events.CONTENT_URI
            putExtra(android.provider.CalendarContract.Events.TITLE, "Job: ${job.title}")
            putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "Client: ${job.clientName}\nStatus: ${job.status}")
            putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, "Client Site")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}