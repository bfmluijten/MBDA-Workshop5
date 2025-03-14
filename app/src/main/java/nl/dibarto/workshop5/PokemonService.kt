package nl.dibarto.workshop5

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.os.HandlerCompat.createAsync
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.runBlocking
import java.util.Random

const val FIVE_SECONDS = 5000

const val POKEMON_NOTIFICATION: Int = 123456
const val POKEMON_CHANNEL = "POKEMON_CHANNEL"

val Context.dataStore by preferencesDataStore("settings")

class PokemonService : Service() {
    private var favorite: Int = 0

    private val binder = PokemonBinder()

    inner class PokemonBinder: Binder() {
        fun setListener(listener: (Int) -> Unit) {
            this@PokemonService.listener = listener
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private var listener: (Int) -> Unit = {}

    fun nextFavorite() {
        favorite = (favorite + 1 + Random().nextInt(2)) % 3
    }

    override fun onCreate() {
        super.onCreate()

        startForeground(POKEMON_NOTIFICATION, getNotification(this))
    }

    private val handler = createAsync(Looper.getMainLooper())

    private var runnable: Runnable = object : Runnable {
        override fun run() {
            nextFavorite()

//            listener(favorite)

            savePreference(intPreferencesKey("favorite"), favorite)

            sendNotification(this@PokemonService)

            handler.postDelayed(this, FIVE_SECONDS.toLong())
        }
    }

    fun <T> savePreference(key: Preferences.Key<T>, value: T) = runBlocking {
        dataStore.edit {
            it[key] = value
        }
    }

    fun sendNotification(context: Context) {
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(POKEMON_NOTIFICATION, getNotification(context))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.removeCallbacks(runnable)

        handler.post(runnable)

        return START_STICKY
    }

    private fun getNotification(context: Context): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)

        val icon = BitmapFactory.decodeResource(context.resources, getIcon(favorite))

        return NotificationCompat.Builder(context, POKEMON_CHANNEL)
            .setContentTitle("Pokemon")
            .setContentText("Favorite: " + getName(favorite))
            .setContentIntent(pending)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(icon)
            .build()
    }

    private fun getIcon(favorite: Int): Int {
        val icons = intArrayOf(R.drawable.bulbasaur, R.drawable.dragonite, R.drawable.pikachu)

        return icons[favorite]
    }

    private fun getName(favorite: Int): String {
        val names = arrayOf("bulbasaur", "dragonite", "pikachu")

        return names[favorite]
    }
}