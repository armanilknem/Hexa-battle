package com.tdt4240.group3.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.network.LobbyService
import com.tdt4240.group3.network.SupabaseClient
import com.tdt4240.group3.network.model.Lobby
import com.tdt4240.group3.network.model.LobbyStatus
import com.tdt4240.group3.network.model.PresenceState
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresSingleDataFlow
import io.github.jan.supabase.realtime.presenceDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.app.clearScreen

class LobbyScreen(
    private val game: Hexa_Battle,
    initialLobby: Lobby
) : KtxScreen {

    private val stage = Stage(ScreenViewport())
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var lobby = initialLobby
    private var channel: RealtimeChannel? = null

    // playerId -> displayName
    private val connectedPlayers = mutableMapOf<String, String>()
    private lateinit var backBtn: VisTextButton

    private lateinit var codeLabel: VisLabel
    private lateinit var countLabel: VisLabel
    private lateinit var playerTable: Table
    private lateinit var startBtn: VisTextButton

    override fun show() {
        if (!VisUI.isLoaded()) VisUI.load()

        backBtn = VisTextButton("BACK")
        codeLabel = VisLabel("CODE: ${lobby.lobbyCode}")
        countLabel = VisLabel("PLAYERS: 0/${lobby.maxPlayerCount}")
        playerTable = Table()
        startBtn = VisTextButton("START GAME")

        Gdx.input.inputProcessor = stage
        setupLayout()
        connectToChannel()
    }

    private fun setupLayout() {
        stage.clear()
        val root = Table().apply {
            setFillParent(true)
            center()
        }

        root.add(backBtn).left().pad(10f).row()
        backBtn.onClick {
            game.setScreen<LobbySelectScreen>()
        }

        root.add(codeLabel).padBottom(10f).row()
        root.add(countLabel).padBottom(20f).row()
        root.add(VisLabel("CONNECTED PLAYERS:")).padBottom(10f).row()
        root.add(playerTable).padBottom(30f).row()

        if (lobby.hostId == game.myPlayerId) {
            root.add(startBtn).width(280f).height(60f).row()
            startBtn.onClick {
                scope.launch {
                    LobbyService.startGame(lobby.id!!, connectedPlayers.keys.toList())
                }
            }
        } else {
            root.add(VisLabel("Waiting for host...")).row()
        }

        stage.addActor(root)
    }


    private fun connectToChannel() {
        val channel = SupabaseClient.client.channel("lobby_${lobby.id}")
        this.channel = channel

        val presenceFlow = channel.presenceDataFlow<PresenceState>()
        presenceFlow.onEach {
            connectedPlayers.clear()
            for (presence in it) {
                connectedPlayers[presence.playerId] = presence.playerName
            }
            refreshPlayerList()
        }.launchIn(scope)

        scope.launch {
            val lobbyFlow = channel.postgresSingleDataFlow(schema="public", table="lobbies", primaryKey=Lobby::id) {
                eq("id", lobby.id!!)
            }
            lobbyFlow.onEach {
                if (it.status === LobbyStatus.PLAYING) {
                    game.setScreen<PlayScreen>()
                }
            }.launchIn(scope)

            channel.subscribe(blockUntilSubscribed = true)
            channel.track(Json.encodeToJsonElement(PresenceState(game.myPlayerId, game.myPlayerName)).jsonObject)
        }
    }

    private fun refreshPlayerList() {
        Gdx.app.postRunnable {
            countLabel.setText("PLAYERS: ${connectedPlayers.size}/${lobby.maxPlayerCount}")
            playerTable.clear()

            connectedPlayers.forEach { (playerId, displayName) ->
                val isHost = playerId == lobby.hostId
                val label = VisLabel(
                    if (isHost) "$displayName (HOST)" else displayName
                )
                playerTable.add(label).padBottom(5f).row()
            }
        }
    }

    override fun render(delta: Float) {
        clearScreen(0.055f, 0.067f, 0.094f, 1f)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun hide() {
        Gdx.input.inputProcessor = null

        scope.launch {
            try {
                channel?.untrack()
                channel?.unsubscribe()
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    override fun dispose() {
        stage.dispose()
        if (VisUI.isLoaded()) VisUI.dispose()
        scope.cancel()
    }
}
