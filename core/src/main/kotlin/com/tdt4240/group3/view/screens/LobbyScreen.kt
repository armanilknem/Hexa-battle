package com.tdt4240.group3.view.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.controller.PlayController
import com.tdt4240.group3.network.LobbyGameStateService
import com.tdt4240.group3.network.LobbyService
import com.tdt4240.group3.network.SupabaseClient
import com.tdt4240.group3.network.model.Lobby
import com.tdt4240.group3.network.model.LobbyStatus
import com.tdt4240.group3.network.model.PresenceState
import com.tdt4240.group3.view.View
import com.tdt4240.group3.view.ViewConfig
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresSingleDataFlow
import io.github.jan.supabase.realtime.presenceDataFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.app.clearScreen
import kotlin.math.sqrt
import kotlin.random.Random

class LobbyScreen(
    private val game: Hexa_Battle,
    initialLobby: Lobby
) : KtxScreen {

    private val stage = Stage(ExtendViewport(ViewConfig.V_WIDTH, ViewConfig.V_HEIGHT))
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val backgroundTexture = Texture(Gdx.files.internal("backgrounds/MenuBackground.png"))

    private var lobby = initialLobby
    private var channel: RealtimeChannel? = null

    private val connectedPlayers = mutableMapOf<String, String>()

    private lateinit var codeLabel: VisLabel
    private lateinit var countLabel: VisLabel
    private lateinit var playerTable: Table
    private lateinit var startBtn: VisTextButton
    private lateinit var backBtn: VisTextButton
    private lateinit var statusLabel: VisLabel

    private var transitioningToPlay = false

    override fun show() {
        if (!VisUI.isLoaded()) VisUI.load()

        backBtn = VisTextButton("BACK").apply { color = Color.BLACK }
        codeLabel = VisLabel("CODE: ${lobby.lobbyCode}").apply { color = Color.BLACK }
        countLabel = VisLabel("PLAYERS: 0/${lobby.maxPlayerCount}").apply { color = Color.BLACK }
        playerTable = Table().apply { color = Color.BLACK }
        startBtn = VisTextButton("START GAME").apply { color = Color.BLACK }
        statusLabel = VisLabel("").apply { color = Color.RED }

        Gdx.input.inputProcessor = stage
        setupLayout()
        connectToChannel()
    }

    private fun setupLayout() {
        stage.clear()
        val root = Table().apply {
            setFillParent(true)
            center()
            background = TextureRegionDrawable(TextureRegion(backgroundTexture))
        }

        val card = Table().apply {
            pad(24f)
            background = VisUI.getSkin().newDrawable("white", Color(1f, 1f, 1f, 0.6f))
        }

        card.add(backBtn).left().pad(10f).row()
        backBtn.onClick { game.setScreen<LobbySelectScreen>() }

        card.add(codeLabel).padBottom(10f).row()
        card.add(countLabel).padBottom(10f).row()
        root.add(statusLabel).padBottom(20f).row()
        card.add(VisLabel("CONNECTED PLAYERS:").apply { color = Color.BLACK }).padBottom(10f).row()
        card.add(playerTable).padBottom(30f).row()

        if (lobby.hostId == game.myPlayerId) {
            card.add(startBtn).width(280f).height(60f).row()
            startBtn.onClick {
                if (connectedPlayers.size > 1 && connectedPlayers.size <= lobby.maxPlayerCount!!) {
                    startBtn.isDisabled = true
                    scope.launch {
                        val sortedOrder = connectedPlayers.keys.sorted()
                        val shuffledOrder = sortedOrder.shuffled(Random(lobby.lobbyCode.hashCode()))

                        LobbyGameStateService.createInitialGameState(
                            lobbyId = lobby.id!!,
                            currentPlayerId = shuffledOrder.first()
                        )

                        LobbyService.startGame(lobby.id!!, shuffledOrder)
                    }
                } else if (connectedPlayers.size <= 1) {
                    statusLabel.setText("Waiting for more players...")
                } else {
                    statusLabel.setText("Too many players!")
                }
            }
        } else {
            card.add(VisLabel("Waiting for host...").apply { color = Color.BLACK }).row()
        }

        root.add(card)
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
            val lobbyFlow = channel.postgresSingleDataFlow(
                schema = "public",
                table = "lobbies",
                primaryKey = Lobby::id
            ) {
                eq("id", lobby.id!!)
            }

            lobbyFlow.onEach { updatedLobby ->
                lobby = updatedLobby

                if (lobby.status == LobbyStatus.PLAYING && !transitioningToPlay) {
                    transitioningToPlay = true

                    val players = LobbyService.getLobbyPlayers(lobby.id!!)
                    val sortedOrder = players.sortedBy { it.playerId }.map { it.playerId }
                    val shuffledOrder = sortedOrder.shuffled(Random(lobby.lobbyCode.hashCode()))

                    Gdx.app.postRunnable {
                        game.resetForNewMatch()

                        val playController = PlayController(game, game.engine)
                        val playScreen = playController.createScreen(
                            lobbyId = lobby.id!!,
                            myPlayerId = game.myPlayerId,
                            playerOrder = shuffledOrder
                        )

                        val cols = 12f
                        val rows = 11f
                        val centerX = GameConstants.HEX_SIZE * (sqrt(3.0).toFloat() * (cols / 2f) + sqrt(3.0).toFloat() / 2f * (rows / 2f))
                        val centerY = GameConstants.HEX_SIZE * (3f / 2f * (rows / 2f)) + 36f

                        playScreen.camera.position.set(centerX, centerY, 0f)
                        playScreen.camera.update()

                        game.view = View(game.batch, game.shapeRenderer, playScreen.camera, game.font)
                        game.engine.addSystem(game.view)

                        game.addScreen(playScreen)
                        game.setScreen<PlayScreen>()
                    }
                }
            }.launchIn(scope)

            channel.subscribe(blockUntilSubscribed = true)
            channel.track(
                Json.encodeToJsonElement(
                    PresenceState(game.myPlayerId, game.myPlayerName)
                ).jsonObject
            )
        }
    }

    private fun refreshPlayerList() {
        Gdx.app.postRunnable {
            countLabel.setText("PLAYERS: ${connectedPlayers.size}/${lobby.maxPlayerCount}")
            playerTable.clear()

            connectedPlayers.forEach { (playerId, displayName) ->
                val isHost = playerId == lobby.hostId
                val label = VisLabel(if (isHost) "$displayName (HOST)" else displayName).apply { color = Color.BLACK }
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
            } catch (_: Exception) {}
        }
    }

    override fun dispose() {
        stage.dispose()
        backgroundTexture.dispose()
        if (VisUI.isLoaded()) VisUI.dispose()
        scope.cancel()
    }

}
