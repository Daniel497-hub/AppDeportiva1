package com.example.ejemplodatabase.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NombreViewModel(
    application : Application
) : AndroidViewModel(
    application
) {
    private val repository: Nombrerepository
    private val _nombres = MutableLiveData<List<NombreEntity>>()

    init {
        val database = NombreDataBase.getDataBase(
            application.applicationContext
        )
        repository = Nombrerepository(database)
    }

    fun insertName(nombre: NombreEntity) {
        viewModelScope.launch {
            repository.insertName(nombre)
            updateNombres()
        }
    }

    fun getNombres(): LiveData<List<NombreEntity>> {
        updateNombres()
        return _nombres
    }

    fun updateNombres() {
        viewModelScope.launch {
            val nombres = repository.getNames()
            _nombres.value = nombres
        }
    }
}
