package com.tdt4240.group3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.tdt4240.group3.model.systems.PlayerSystem
import com.tdt4240.group3.view.screens.HowToPlayScreen
import com.tdt4240.group3.view.View
import com.tdt4240.group3.network.PlayerService
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import com.tdt4240.group3.view.screens.MenuScreen
import com.tdt4240.group3.view.screens.OptionsScreen
import com.tdt4240.group3.view.screens.PlayScreen
import com.tdt4240.group3.view.screens.WinScreen
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.view.screens.LeaderboardScreen
import com.tdt4240.group3.view.screens.LobbySelectScreen
import ktx.assets.disposeSafely
import java.util.UUID
import kotlinx.coroutines.launch

class Hexa_Battle : KtxGame<KtxScreen>() {

    lateinit var engine: Engine
    lateinit var view: View
    lateinit var batch: SpriteBatch
    lateinit var font: BitmapFont
    lateinit var shapeRenderer: ShapeRenderer

    var myPlayerId: String = ""
        private set
    var myPlayerName: String = ""
        private set
    var myTeam: Team = Team.NONE

    companion object {
        const val WIDTH = 640
        const val HEIGHT = 480
        const val TITLE = "Hexa Battle"
    }

    override fun create() {
        KtxAsync.initiate()
        setupPlayerIdentity()

        batch = SpriteBatch()
        font = BitmapFont()
        shapeRenderer = ShapeRenderer()

        engine = Engine()
        engine.addSystem(PlayerSystem())

        addScreen(MenuScreen(this))
        addScreen(LobbySelectScreen(this))
        addScreen(HowToPlayScreen(this))
        addScreen(LeaderboardScreen(this))
        addScreen(OptionsScreen(this))
        addScreen(WinScreen(this))

        setScreen<MenuScreen>()
    }

    private fun setupPlayerIdentity() {
        val prefs = Gdx.app.getPreferences("HexaBattlePrefs")
        val savedId = prefs.getString("player_id", "")

        KtxAsync.launch {
            if (savedId.isEmpty()) {
                val newId = UUID.randomUUID().toString()
                val defaultName = "Guest${(1000..9999).random()}"
                val player = PlayerService.getOrCreatePlayer(newId, defaultName)
                prefs.putString("player_id", newId)
                prefs.flush()
                myPlayerId = newId
                myPlayerName = player?.displayName ?: defaultName
            } else {
                val player = PlayerService.getOrCreatePlayer(savedId)
                myPlayerId = savedId
                myPlayerName = player?.displayName ?: "Guest"
            }
        }
    }

    fun resetForNewMatch() {
        if (containsScreen<PlayScreen>()) {
            removeScreen<PlayScreen>()
        }

        if (::view.isInitialized) {
            view.disposeSafely()
        }

        engine = Engine()
        engine.addSystem(PlayerSystem())
    }

    override fun dispose() {
        if (::view.isInitialized) {
            view.disposeSafely()
        }
        font.disposeSafely()
        batch.disposeSafely()
        shapeRenderer.disposeSafely()
        super.dispose()
    }

    fun setPlayerName(name: String) {
        myPlayerName = name
    }
}
