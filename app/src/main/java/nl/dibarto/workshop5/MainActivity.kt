package nl.dibarto.workshop5

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.dibarto.workshop5.ui.theme.Workshop5Theme

class MainActivity : ComponentActivity() {
    private var favorite by mutableIntStateOf(0)

    private var listenPreferenceJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(POKEMON_CHANNEL, "Name", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            ActivityCompat.requestPermissions(this, permissions, 1)
        }

        enableEdgeToEdge()

        setContent {
            Workshop5Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Spacer(modifier = Modifier.weight(1.0f))
                        Image(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxWidth()
                                .size(300.dp),
                            painter = painterResource(getIcon(favorite)),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.weight(1.0f))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        bindService(Intent(this, PokemonService::class.java), serviceConnection, BIND_AUTO_CREATE)

        startService(Intent(this, PokemonService::class.java))

        listenPreferenceJob = lifecycleScope.launch {
            dataStore.data.map {
                it[intPreferencesKey("favorite")]
            }.cancellable().collect {
                favorite = it ?: 0
            }
        }
    }

    override fun onPause() {
        super.onPause()

        unbindService(serviceConnection)

        listenPreferenceJob?.cancel()
    }

    private var serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PokemonService.PokemonBinder

            binder.setListener { favorite = it }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Workshop5Theme {
        Greeting("Android")
    }
}