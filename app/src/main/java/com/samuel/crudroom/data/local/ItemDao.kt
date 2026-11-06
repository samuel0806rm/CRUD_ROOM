package com.samuel.crudroom.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object): define las operaciones sobre la tabla.
 * Room genera la implementación en tiempo de compilación mediante KSP.
 *
 * - Las lecturas devuelven Flow: emiten de nuevo cada vez que cambian los datos.
 * - Las escrituras son suspend: se ejecutan sin bloquear el hilo principal.
 */
@Dao
interface ItemDao {

    /** CREATE: inserta un ítem y devuelve el id generado por SQLite. */
    @Insert
    suspend fun insertarItem(item: ItemEntity): Long

    /** READ: observa todos los ítems, ordenados por id. */
    @Query("SELECT * FROM items_trabajo ORDER BY id ASC")
    fun obtenerItems(): Flow<List<ItemEntity>>

    /** READ: observa un ítem por su id; emite null si no existe. */
    @Query("SELECT * FROM items_trabajo WHERE id = :id")
    fun obtenerItemPorId(id: Int): Flow<ItemEntity?>

    /** UPDATE: actualiza la fila cuya clave primaria coincide con item.id. */
    @Update
    suspend fun actualizarItem(item: ItemEntity)

    /** DELETE: elimina la fila cuya clave primaria coincide con item.id. */
    @Delete
    suspend fun eliminarItem(item: ItemEntity)
}
