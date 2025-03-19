package nl.dibarto.workshop5

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.fillMaxSize
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PokemonWidget : GlanceAppWidget() {
    private var favorite by mutableIntStateOf(0)

    override suspend fun provideGlance(context: Context, id: GlanceId) = coroutineScope {
        launch {
            context.dataStore.data.map {
                it[intPreferencesKey("favorite")]
            }.cancellable().collect {
                favorite = it ?: 0
            }
        }

        provideContent {
            Image(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(onClick = actionStartActivity<MainActivity>()),
                provider = ImageProvider(
                    when (favorite) {
                        0 -> R.drawable.bulbasaur
                        1 -> R.drawable.dragonite
                        else -> R.drawable.pikachu
                    }
                ),
                contentDescription = ""
            )
        }
    }
}