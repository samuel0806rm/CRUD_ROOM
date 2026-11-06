package com.samuel.crudroom.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad: representa una fila de la tabla "items_trabajo".
 * Cada propiedad de la clase corresponde a una columna.
 */
@Entity(tableName = "items_trabajo")
data class ItemEntity(
    // Con autoGenerate, el valor 0 le indica a Room que SQLite debe asignar el id al insertar.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val descripcion: String
)
