package nl.dibarto.workshop5

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.GlanceStateDefinition
import kotlinx.coroutines.coroutineScope
import java.io.File

class PokemonWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) = coroutineScope {
        provideContent {
            val favorite = currentState<Preferences>()[intPreferencesKey("favorite")] ?: 0

            Image(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(onClick = actionStartActivity<MainActivity>()),
                provider = ImageProvider(getIcon(favorite)),
                contentDescription = ""
            )
        }
    }

    override val stateDefinition: GlanceStateDefinition<Preferences>?
        get() = object : GlanceStateDefinition<Preferences> {
            override suspend fun getDataStore(
                context: Context,
                fileKey: String
            ): DataStore<Preferences> {
                return context.dataStore
            }

            override fun getLocation(
                context: Context,
                fileKey: String
            ): File {
                TODO("Not yet implemented")
            }
        }
}