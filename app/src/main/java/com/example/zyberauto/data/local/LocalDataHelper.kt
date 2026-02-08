package com.example.zyberauto.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataHelper @Inject constructor(
    private val context: Context
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    
    // Changing folder from "data" to "json_storage" as requested
    private val dataDir: File by lazy {
        File(context.filesDir, "json_storage").apply {
            if (!exists()) {
                mkdirs()
                copyAssetsToStorage(this)
            }
        }
    }

    private fun copyAssetsToStorage(targetDir: File) {
        try {
            val assetManager = context.assets
            // List files in assets/json_storage
            val assets = assetManager.list("json_storage") ?: return
            
            for (filename in assets) {
                try {
                    val outFile = File(targetDir, filename)
                    // Only copy if it doesn't exist to avoid overwriting user data
                    if (!outFile.exists()) {
                        assetManager.open("json_storage/$filename").use { inputStream ->
                            java.io.FileOutputStream(outFile).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun <T> readList(fileName: String, clazz: Class<T>): List<T> {
        // Trigger lazy initialization to ensure directory and assets exist
        val dir = dataDir
        val file = File(dir, fileName)
        if (!file.exists()) return emptyList()

        return try {
            val type = TypeToken.getParameterized(List::class.java, clazz).type
            FileReader(file).use { reader ->
                gson.fromJson<List<T>>(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun <T> writeList(fileName: String, list: List<T>) {
        val file = File(dataDir, fileName)
        try {
            FileWriter(file).use { writer ->
                gson.toJson(list, writer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun <T> addItem(fileName: String, item: T, clazz: Class<T>) {
        val currentList = readList(fileName, clazz).toMutableList()
        currentList.add(item)
        writeList(fileName, currentList)
    }

    suspend fun <T> removeItem(fileName: String, clazz: Class<T>, predicate: (T) -> Boolean) {
        val currentList = readList(fileName, clazz).toMutableList()
        currentList.removeAll(predicate)
        writeList(fileName, currentList)
    }

    suspend fun <T> updateItem(
        fileName: String, 
        clazz: Class<T>, 
        predicate: (T) -> Boolean, 
        update: (T) -> T
    ) {
        val currentList = readList(fileName, clazz).toMutableList()
        val index = currentList.indexOfFirst(predicate)
        if (index != -1) {
            currentList[index] = update(currentList[index])
            writeList(fileName, currentList)
        }
    }
}
