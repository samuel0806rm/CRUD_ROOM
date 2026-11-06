package com.samuel.crudroom.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuel.crudroom.R
import com.samuel.crudroom.data.local.ItemEntity
import com.samuel.crudroom.ui.theme.CrudRoomTheme
import com.samuel.crudroom.ui.viewmodel.FormularioUiState
import com.samuel.crudroom.ui.viewmodel.ItemViewModel

/**
 * Pantalla principal. Observa los StateFlow del ViewModel y se redibuja cuando cambian.
 * collectAsStateWithLifecycle deja de observar mientras la app está en segundo plano.
 */
@Composable
fun PantallaItems(viewModel: ItemViewModel) {
    val listaUiState by viewModel.listaUiState.collectAsStateWithLifecycle()
    val formulario by viewModel.formularioUiState.collectAsStateWithLifecycle()
    val itemPorEliminar by viewModel.itemPorEliminar.collectAsStateWithLifecycle()

    ContenidoPantalla(
        items = listaUiState.items,
        cargando = listaUiState.cargando,
        formulario = formulario,
        alCambiarTitulo = viewModel::alCambiarTitulo,
        alCambiarDescripcion = viewModel::alCambiarDescripcion,
        alGuardar = viewModel::guardar,
        alCancelarEdicion = viewModel::cancelarEdicion,
        alEditar = { item -> viewModel.iniciarEdicion(item.id) },
        alEliminar = viewModel::solicitarEliminacion
    )

    // El diálogo aparece solo cuando hay un ítem pendiente de confirmación.
    itemPorEliminar?.let { item ->
        DialogoConfirmarEliminacion(
            item = item,
            alConfirmar = viewModel::confirmarEliminacion,
            alCancelar = viewModel::cancelarEliminacion
        )
    }
}

/**
 * Contenido sin dependencia del ViewModel: recibe el estado y notifica los eventos por parámetros.
 * Esto se conoce como "state hoisting" y permite usar este composable en las vistas previas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoPantalla(
    items: List<ItemEntity>,
    cargando: Boolean,
    formulario: FormularioUiState,
    alCambiarTitulo: (String) -> Unit,
    alCambiarDescripcion: (String) -> Unit,
    alGuardar: () -> Unit,
    alCancelarEdicion: () -> Unit,
    alEditar: (ItemEntity) -> Unit,
    alEliminar: (ItemEntity) -> Unit
) {
    val estadoLista = rememberLazyListState()

    // Al pulsar editar en una tarjeta lejana, sube la lista para que el formulario quede visible.
    val idEnEdicion = formulario.itemEnEdicion?.id
    LaunchedEffect(idEnEdicion) {
        if (idEnEdicion != null) estadoLista.animateScrollToItem(0)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
    ) { paddingInterno ->
        // El formulario forma parte de la lista para que todo se pueda desplazar,
        // incluso en pantallas pequeñas, en horizontal o con el teclado abierto.
        LazyColumn(
            state = estadoLista,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "formulario") {
                FormularioItem(
                    formulario = formulario,
                    alCambiarTitulo = alCambiarTitulo,
                    alCambiarDescripcion = alCambiarDescripcion,
                    alGuardar = alGuardar,
                    alCancelarEdicion = alCancelarEdicion
                )
            }

            if (items.isEmpty() && !cargando) {
                item(key = "vacio") {
                    Text(
                        text = stringResource(R.string.lista_vacia),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // La clave estable ayuda a Compose a identificar cada tarjeta cuando cambia la lista.
            items(items = items, key = { item -> item.id }) { item ->
                TarjetaItem(
                    item = item,
                    alEditar = { alEditar(item) },
                    alEliminar = { alEliminar(item) }
                )
            }
        }
    }
}

/** Formulario de creación y edición de ítems. */
@Composable
private fun FormularioItem(
    formulario: FormularioUiState,
    alCambiarTitulo: (String) -> Unit,
    alCambiarDescripcion: (String) -> Unit,
    alGuardar: () -> Unit,
    alCancelarEdicion: () -> Unit
) {
    val estaEditando = formulario.itemEnEdicion != null

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = formulario.titulo,
            onValueChange = alCambiarTitulo,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.campo_titulo)) },
            singleLine = true,
            isError = formulario.tituloInvalido,
            supportingText = if (formulario.tituloInvalido) {
                { Text(stringResource(R.string.error_titulo_vacio)) }
            } else {
                null
            }
        )

        OutlinedTextField(
            value = formulario.descripcion,
            onValueChange = alCambiarDescripcion,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.campo_descripcion)) },
            minLines = 2
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = alGuardar, modifier = Modifier.weight(1f)) {
                Text(stringResource(if (estaEditando) R.string.boton_actualizar else R.string.boton_guardar))
            }
            if (estaEditando) {
                OutlinedButton(onClick = alCancelarEdicion, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.boton_cancelar))
                }
            }
        }
    }
}

/** Tarjeta que muestra un ítem con sus botones de editar y eliminar. */
@Composable
private fun TarjetaItem(
    item: ItemEntity,
    alEditar: () -> Unit,
    alEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.etiqueta_id, item.id),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(text = item.titulo, style = MaterialTheme.typography.titleMedium)
                if (item.descripcion.isNotEmpty()) {
                    Text(text = item.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = alEditar) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.accion_editar, item.titulo)
                )
            }
            IconButton(onClick = alEliminar) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.accion_eliminar, item.titulo)
                )
            }
        }
    }
}

/** Diálogo que pide confirmación antes de eliminar un ítem. */
@Composable
private fun DialogoConfirmarEliminacion(
    item: ItemEntity,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text(stringResource(R.string.dialogo_titulo)) },
        text = { Text(stringResource(R.string.dialogo_mensaje, item.titulo)) },
        confirmButton = {
            TextButton(onClick = alConfirmar) {
                Text(stringResource(R.string.boton_eliminar))
            }
        },
        dismissButton = {
            TextButton(onClick = alCancelar) {
                Text(stringResource(R.string.boton_cancelar))
            }
        }
    )
}

// ---------- Vistas previas: se ven en Android Studio con el modo "Split" o "Design" ----------

private val itemsDeEjemplo = listOf(
    ItemEntity(1, "Comprar libros", "Cálculo y física"),
    ItemEntity(2, "Entregar informe", "Trabajo de Room")
)

/** Pantalla con datos de ejemplo; en la vista previa los eventos no hacen nada. */
@Composable
private fun PantallaDeEjemplo(
    items: List<ItemEntity> = itemsDeEjemplo,
    formulario: FormularioUiState = FormularioUiState()
) {
    CrudRoomTheme {
        ContenidoPantalla(
            items = items,
            cargando = false,
            formulario = formulario,
            alCambiarTitulo = {},
            alCambiarDescripcion = {},
            alGuardar = {},
            alCancelarEdicion = {},
            alEditar = {},
            alEliminar = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Lista con ítems")
@Composable
private fun VistaPreviaConItems() {
    PantallaDeEjemplo()
}

@Preview(showBackground = true, showSystemUi = true, name = "Lista vacía")
@Composable
private fun VistaPreviaVacia() {
    PantallaDeEjemplo(items = emptyList())
}

@Preview(showBackground = true, showSystemUi = true, name = "Validación de título")
@Composable
private fun VistaPreviaValidacion() {
    PantallaDeEjemplo(
        items = emptyList(),
        formulario = FormularioUiState(tituloInvalido = true)
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "Modo edición")
@Composable
private fun VistaPreviaEdicion() {
    val item = itemsDeEjemplo.first()
    PantallaDeEjemplo(
        formulario = FormularioUiState(
            titulo = item.titulo,
            descripcion = item.descripcion,
            itemEnEdicion = item
        )
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Tema oscuro"
)
@Composable
private fun VistaPreviaTemaOscuro() {
    PantallaDeEjemplo()
}

@Preview(showBackground = true, name = "Tarjeta con texto largo")
@Composable
private fun VistaPreviaTarjeta() {
    CrudRoomTheme {
        TarjetaItem(
            item = ItemEntity(
                id = 3,
                titulo = "Preparar la exposición final de la materia",
                descripcion = "Revisar las diapositivas, ensayar la demostración del CRUD " +
                    "y llevar la aplicación instalada en el celular."
            ),
            alEditar = {},
            alEliminar = {}
        )
    }
}

@Preview(showBackground = true, name = "Diálogo de eliminación")
@Composable
private fun VistaPreviaDialogo() {
    CrudRoomTheme {
        DialogoConfirmarEliminacion(
            item = itemsDeEjemplo.last(),
            alConfirmar = {},
            alCancelar = {}
        )
    }
}
