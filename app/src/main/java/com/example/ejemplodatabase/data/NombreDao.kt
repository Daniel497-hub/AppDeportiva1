package com.example.ejemplodatabase.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NombreDao {
    @Insert
    suspend fun insertNombre(nombre : NombreEntity)

    @Query("SELECT * FROM nombres")
    suspend fun getNombres() : List<NombreEntity>

}