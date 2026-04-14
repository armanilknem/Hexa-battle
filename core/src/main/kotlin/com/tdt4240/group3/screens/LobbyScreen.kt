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
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
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

    private lateinit var codeLabel: VisLabel
    private lateinit var countLabel: VisLabel
    private lateinit var playerTable: Table
    private lateinit var startBtn: VisTextButton

    override fun show() {
        if (!VisUI.isLoaded()) VisUI.load()

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
        val ch = SupabaseClient.client.channel("lobby_${lobby.id}") {
            presence { key = game.myPlayerId }
        }
        channel = ch

        ch.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "lobbies"
            filter("id", FilterOperator.EQ, "${lobby.id}")
        }
            .onEach { action ->
                lobby = action.decodeRecord()

                if (lobby.status == LobbyStatus.PLAYING) {
                    Gdx.app.postRunnable {
                        game.setScreen<PlayScreen>()
                    }
                }
            }
            .launchIn(scope)

        // Presence updates (joins/leaves only)
        ch.presenceChangeFlow()
            .onEach { action ->

                // Joins
                action.joins.forEach { (key, presence) ->
                    val name = presence.state["playerName"]
                        ?.let { it as? JsonPrimitive }
                        ?.contentOrNull
                        ?: key

                    connectedPlayers[key] = name
                }

                // Leaves
                action.leaves.keys.forEach { key ->
                    connectedPlayers.remove(key)
                }

                refreshPlayerList()
            }
            .launchIn(scope)

        // --- Now subscribe and track ---
        scope.launch {
            ch.subscribe(blockUntilSubscribed = true)

            ch.track(
                buildJsonObject {
                    put("playerId", game.myPlayerId)
                    put("playerName", game.myPlayerName)
                }
            )
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
