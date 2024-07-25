package com.bassmd.modloader

import android.annotation.SuppressLint
import android.util.Log
import androidx.collection.ArrayMap
import net.fornwall.jelf.ElfDynamicSection
import net.fornwall.jelf.ElfFile
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

import kotlin.jvm.Throws;

class LibLoader(libraryPaths: String) {

    private val alreadyLoadedLibs = mutableListOf<String>()
    private val dirCache = mutableListOf<ArrayMap<String, File>>()

    init {
        libraryPaths.split(":").forEach { path ->
                val caching = ArrayMap<String, File>()
            val filesInDir = File(path).listFiles { pathname -> pathname.isFile && pathname.exists() }
            if (filesInDir != null) {
                for (file in filesInDir) {
                    caching[file.name] = file
                }
                dirCache.add(caching)
            } else {
                Log.w("ElfLoader", "Omitted directory during initialization: $path")
            }
        }
        BufferedReader(FileReader(File("/proc/self/maps"))).use { selfMapsReader ->
                var mapLine: String?
            while (selfMapsReader.readLine().also { mapLine = it } != null) {
                if (mapLine!!.endsWith(".so")) {
                    val map = mapLine!!.substring(mapLine!!.lastIndexOf("/") + 1)
                    addLoaded(map)
                }
            }
        }
    }

    private fun addLoaded(libName: String) {
        if (!alreadyLoadedLibs.contains(libName)) {
            alreadyLoadedLibs.add(libName)
        }
    }

    @Throws(IOException::class)
    fun loadLib(libName: String) {
        if (alreadyLoadedLibs.contains(libName)) return
                val library = getLibrary(libName)
        if (library == null) {
            Log.w("ElfSequencer", "Library $libName not found in search paths")
            return
        }
        val file = ElfFile.from(library)
        val section = file.firstSectionByType(ElfDynamicSection::class.java)
        val needed = section.neededLibraries
        for (neededLibrary in needed) {
            Log.i("ElfSequencer", "Needed: $neededLibrary")
            loadLib(neededLibrary)
        }
        Log.i("ElfLoader", "Loading library ${library.name}")
        loadNative(library.absolutePath)
        alreadyLoadedLibs.add(libName)
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    private fun loadNative(absolutePath: String) {
        System.load(absolutePath)
    }

    private fun getLibrary(libName: String): File? {
        for (fileCache in dirCache) {
            if (fileCache.containsKey(libName)) return fileCache[libName]
        }
        return null
    }
}
