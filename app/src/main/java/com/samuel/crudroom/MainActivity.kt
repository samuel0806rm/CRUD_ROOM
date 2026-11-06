package com.samuel.crudroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.samuel.crudroom.data.local.AppDatabase
import com.samuel.crudroom.data.repository.ItemRepository
import com.samuel.crudroom.ui.screens.PantallaItems
import com.samuel.crudroom.ui.theme.CrudRoomTheme
import com.samuel.crudroom.ui.viewmodel.ItemViewModel
import com.samuel.crudroom.ui.viewmodel.ItemViewModelFactory

/** Única actividad de la app: conecta las capas y muestra la interfaz de Compose. */
class MainActivity : ComponentActivity() {

    // La cadena de dependencias se arma a mano: Database -> DAO -> Repository -> ViewModel.
    private val itemViewModel: ItemViewModel by viewModels {
        val dao = AppDatabase.obtenerInstancia(applicationContext).itemDao()
        ItemViewModelFactory(ItemRepository(dao))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrudRoomTheme {
                PantallaItems(viewModel = itemViewModel)
            }
        }
    }
}
