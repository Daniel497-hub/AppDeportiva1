package com.example.ejemplodatabase.data

import androidx.room.RoomDatabase

class Nombrerepository(private val database: NombreDataBase) {

    private val nombreDao = database.nombreDao()

    // Método para insertar un nuevo jugador
    suspend fun insertName(nombre: NombreEntity) {
        nombreDao.insertNombre(nombre)
    }

    // Método para obtener todos los jugadores
    suspend fun getNames(): List<NombreEntity> {
        return nombreDao.getNombres()
    }
}