package com.tdt4240.group3.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.network.LobbyService
import com.tdt4240.group3.network.SupabaseClient
import com.tdt4240.group3.network.model.Lobby
import com.tdt4240.group3.network.model.LobbyStatus
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.app.clearScreen

class LobbyScreen(private val game: Hexa_Battle, initialLobby: Lobby) : KtxScreen {
    private val stage = Stage(ScreenViewport())
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var lobby = initialLobby

    private val codeLabel = VisLabel("CODE: ${initialLobby.lobbyCode}")
    private val countLabel = VisLabel("PLAYERS: ${initialLobby.playerCount}/${initialLobby.maxPlayerCount}")
    private val startBtn = VisTextButton("START GAME")

    override fun show() {
        Gdx.input.inputProcessor = stage
        setupLayout()
        listenToLobbyUpdates()
    }

    private fun setupLayout() {
        stage.clear()
        val root = Table().apply { setFillParent(true); center() }

        root.add(codeLabel).padBottom(10f).row()
        root.add(countLabel).padBottom(40f).row()

        if (lobby.hostId == game.myPlayerId) {
            startBtn.onClick {
                scope.launch {
                    LobbyService.updateStatus(lobby.id!!, LobbyStatus.PLAYING)
                }
            }
            root.add(startBtn).width(280f).height(60f).row()
        } else {
            root.add(VisLabel("Waiting for host...")).row()
        }

        stage.addActor(root)
    }

    private fun listenToLobbyUpdates() {
        val myChannel = SupabaseClient.client.channel("lobby_updates_${lobby.id}")

        val flow = myChannel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "lobbies"
            filter("id", FilterOperator.EQ, "${lobby.id}")
        }

        // 3. Listen and update
        flow.onEach { action ->
            lobby = action.decodeRecord<Lobby>()

            Gdx.app.postRunnable {
                countLabel.setText("PLAYERS: ${lobby.playerCount}/${lobby.maxPlayerCount}")
                if (lobby.status == LobbyStatus.PLAYING) {
                    game.setScreen<PlayScreen>()
                }
            }
        }.launchIn(scope)

        scope.launch {
            myChannel.subscribe()
        }
    }

    override fun render(delta: Float) {
        clearScreen(0.055f, 0.067f, 0.094f, 1f)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) = stage.viewport.update(width, height, true)

    override fun hide() {
        Gdx.input.inputProcessor = null
        scope.launch {
            val channel = SupabaseClient.client.channel("lobby_updates_${lobby.id}")
            SupabaseClient.client.realtime.removeChannel(channel)
        }
    }

    override fun dispose() {
        stage.dispose()
        scope.cancel()
    }
}
