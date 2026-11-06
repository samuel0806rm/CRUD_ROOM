package com.samuel.crudroom.data.repository

import com.samuel.crudroom.data.local.ItemDao
import com.samuel.crudroom.data.local.ItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio: intermediario entre el ViewModel y la fuente de datos.
 * El ViewModel no conoce a Room; si los datos cambiaran de origen, solo se modificaría esta clase.
 */
class ItemRepository(private val itemDao: ItemDao) {

    suspend fun insertarItem(item: ItemEntity): Long = itemDao.insertarItem(item)

    fun obtenerItems(): Flow<List<ItemEntity>> = itemDao.obtenerItems()

    fun obtenerItemPorId(id: Int): Flow<ItemEntity?> = itemDao.obtenerItemPorId(id)

    suspend fun actualizarItem(item: ItemEntity) = itemDao.actualizarItem(item)

    suspend fun eliminarItem(item: ItemEntity) = itemDao.eliminarItem(item)
}
