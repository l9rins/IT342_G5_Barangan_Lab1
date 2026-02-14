package com.l9rins.trademate.data

import kotlinx.coroutines.flow.Flow

class ClientRepository(private val clientDao: ClientDao) {

    val allClients: Flow<List<Client>> = clientDao.getAllClients()

    suspend fun insert(client: Client) {
        clientDao.insertClient(client)
    }

    // NEW: Delete
    suspend fun delete(client: Client) {
        clientDao.deleteClient(client)
    }
}