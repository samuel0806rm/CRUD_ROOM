package com.samuel.crudroom.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuel.crudroom.data.local.ItemEntity
import com.samuel.crudroom.data.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Etiqueta para filtrar en Logcat las operaciones del CRUD. */
private const val TAG = "CRUD"

/**
 * Estado de la lista. "cargando" evita mostrar "No hay ítems registrados"
 * durante el instante en que Room todavía no ha entregado la primera consulta.
 */
data class ListaUiState(
    val items: List<ItemEntity> = emptyList(),
    val cargando: Boolean = true
)

/** Estado del formulario. Si itemEnEdicion no es null, el formulario está en modo edición. */
data class FormularioUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val itemEnEdicion: ItemEntity? = null,
    val tituloInvalido: Boolean = false
)

/**
 * ViewModel: contiene el estado de la pantalla y la lógica del CRUD.
 * Sobrevive a los cambios de configuración, como la rotación del dispositivo.
 */
class ItemViewModel(private val repositorio: ItemRepository) : ViewModel() {

    /**
     * READ: convierte el Flow de Room en un StateFlow que la interfaz puede observar.
     * WhileSubscribed(5000) mantiene la consulta activa 5 segundos después de que la
     * interfaz deje de observar; así, una rotación de pantalla no reinicia la consulta.
     */
    val listaUiState: StateFlow<ListaUiState> = repositorio.obtenerItems()
        .onEach { items -> Log.d(TAG, "READ: la lista tiene ${items.size} ítem(s)") }
        .map { items -> ListaUiState(items = items, cargando = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListaUiState()
        )

    private val _formularioUiState = MutableStateFlow(FormularioUiState())
    val formularioUiState: StateFlow<FormularioUiState> = _formularioUiState.asStateFlow()

    /** Ítem pendiente de confirmación; si no es null, la interfaz muestra el diálogo. */
    private val _itemPorEliminar = MutableStateFlow<ItemEntity?>(null)
    val itemPorEliminar: StateFlow<ItemEntity?> = _itemPorEliminar.asStateFlow()

    fun alCambiarTitulo(titulo: String) {
        _formularioUiState.update { it.copy(titulo = titulo, tituloInvalido = false) }
    }

    fun alCambiarDescripcion(descripcion: String) {
        _formularioUiState.update { it.copy(descripcion = descripcion) }
    }

    /** Valida el formulario y ejecuta CREATE o UPDATE según el modo en que esté. */
    fun guardar() {
        val formulario = _formularioUiState.value
        val titulo = formulario.titulo.trim()
        val descripcion = formulario.descripcion.trim()

        if (titulo.isEmpty()) {
            Log.d(TAG, "VALIDACIÓN: no se guardó porque el título está vacío")
            _formularioUiState.update { it.copy(tituloInvalido = true) }
            return
        }

        val itemEnEdicion = formulario.itemEnEdicion
        // Se limpia antes de lanzar la corrutina para que un doble toque no inserte dos veces.
        limpiarFormulario()

        viewModelScope.launch {
            if (itemEnEdicion == null) {
                crearItem(titulo, descripcion)
            } else {
                actualizarItem(itemEnEdicion.copy(titulo = titulo, descripcion = descripcion))
            }
        }
    }

    /** READ por id: consulta el ítem en la base de datos y lo carga en el formulario. */
    fun iniciarEdicion(id: Int) {
        viewModelScope.launch {
            // first() toma el valor actual del Flow y deja de observarlo.
            val item = repositorio.obtenerItemPorId(id).first()
            Log.d(TAG, "READ: obtenerItemPorId($id) -> $item")
            if (item != null) {
                _formularioUiState.value = FormularioUiState(
                    titulo = item.titulo,
                    descripcion = item.descripcion,
                    itemEnEdicion = item
                )
            }
        }
    }

    fun cancelarEdicion() {
        Log.d(TAG, "UPDATE: edición cancelada")
        limpiarFormulario()
    }

    fun solicitarEliminacion(item: ItemEntity) {
        _itemPorEliminar.value = item
    }

    fun cancelarEliminacion() {
        _itemPorEliminar.value = null
    }

    /** DELETE: se ejecuta solo después de que el usuario confirma en el diálogo. */
    fun confirmarEliminacion() {
        val item = _itemPorEliminar.value ?: return
        _itemPorEliminar.value = null

        // Si el ítem eliminado se estaba editando, el formulario ya no tiene nada que actualizar.
        if (_formularioUiState.value.itemEnEdicion?.id == item.id) {
            limpiarFormulario()
        }

        viewModelScope.launch {
            repositorio.eliminarItem(item)
            Log.d(TAG, "DELETE: eliminado $item")
        }
    }

    private suspend fun crearItem(titulo: String, descripcion: String) {
        val nuevoItem = ItemEntity(titulo = titulo, descripcion = descripcion)
        val idGenerado = repositorio.insertarItem(nuevoItem)
        Log.d(TAG, "CREATE: insertado ${nuevoItem.copy(id = idGenerado.toInt())}")
    }

    private suspend fun actualizarItem(item: ItemEntity) {
        repositorio.actualizarItem(item)
        Log.d(TAG, "UPDATE: actualizado $item")
    }

    private fun limpiarFormulario() {
        _formularioUiState.value = FormularioUiState()
    }
}
