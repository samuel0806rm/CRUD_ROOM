package com.samuel.crudroom.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Punto de acceso a la base de datos SQLite.
 * exportSchema = false porque este proyecto no maneja migraciones entre versiones.
 */
@Database(entities = [ItemEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        private const val NOMBRE_BD = "db_trabajo_room"

        // @Volatile hace que el valor escrito por un hilo sea visible de inmediato para los demás.
        @Volatile
        private var instancia: AppDatabase? = null

        /**
         * Singleton: abrir una base de datos es costoso, así que toda la app comparte una instancia.
         * El bloque synchronized evita que dos hilos la creen al mismo tiempo.
         */
        fun obtenerInstancia(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE_BD
                ).build().also { instancia = it }
            }
        }
    }
}
