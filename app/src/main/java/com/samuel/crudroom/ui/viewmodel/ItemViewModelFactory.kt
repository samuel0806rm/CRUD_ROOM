package com.samuel.crudroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.samuel.crudroom.data.repository.ItemRepository

/**
 * Fábrica del ViewModel. Es necesaria porque ItemViewModel recibe un parámetro en su
 * constructor y Android, por defecto, solo sabe crear ViewModels sin parámetros.
 */
class ItemViewModelFactory(
    private val repositorio: ItemRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemViewModel(repositorio) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
