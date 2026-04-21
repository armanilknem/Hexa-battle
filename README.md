# Hexa Battle

A turn-based multiplayer hex-grid strategy game built with LibGDX and Kotlin for the TDT4240 Software Architecture course at NTNU.

## Architecture

The project follows an **MVC + ECS** architecture:

- **Model**: The game world is modelled as an [Ashley](https://github.com/libgdx/ashley) Entity Component System. Entities (tiles, cities, troops, game state) hold pure data via `Component` classes. `System` classes contain all game logic (movement, collision, territory, turns, win detection).
- **View**: LibGDX `Screen` classes own the UI. `View.kt` is an Ashley `EntitySystem` that reads component state and renders everything. Sub-states (`PlayerTurnState`, `EnemyTurnState`, `PauseState`) implement the **State pattern** via `PlaySubState`.
- **Controller**: Controller classes (`PlayController`, `TurnController`, `SelectionController`, `PauseController`) mediate between input/network events and ECS mutations.

**Additional patterns:**
- **Factory pattern**: `TileFactory`, `CityFactory`, `CapitalFactory`, `TroopFactory` each implement `EntityFactory<Config>` and encapsulate entity creation.
- **Supabase**: Real-time multiplayer via PostgreSQL + Realtime channels. `MultiplayerManager` bridges network events into ECS component mutations.

## Module structure

| Module   | Purpose |
|----------|---------|
| `core`   | All shared game code: model (ECS), view (screens/renderer), controller, network, config |
| `android`| Android launcher (`AndroidLauncher`) targeting API 23–36 |
| `lwjgl3` | Desktop launcher (`Lwjgl3Launcher`) using the LWJGL3 backend |

Key packages under `core/src/main/kotlin/com/tdt4240/group3/`:

```
config/       - GameConstants, ZIndex, MapData, unit configs
model/        — ECS components, systems, entities (factories), MapGenerator
view/         — Screens, View renderer, state machine, style registries
controller/   — PlayController, TurnController, SelectionController, PauseController
network/      — Supabase client, service classes (LobbyService, LobbyGameStateService, etc.)
```

## Prerequisites

- JDK 17 or later
- Android SDK (for Android builds), set `ANDROID_HOME` or configure in `local.properties`
- Gradle wrapper included (`gradlew` / `gradlew.bat`)

## Running the game

**Desktop:**
```bash
./gradlew :lwjgl3:run
```

**Android**: first start an Android emulator or connect an Android phone. The
recommended emulator setup is **Medium Phone API 36.1**. Once the emulator or
phone is running and connected through the Android Debug Bridge (ADB), run:

```bash
./gradlew :android:installDebug
```
You can then click on the application with the two swords clashing icon to start the game.

**Build a runnable JAR:**
```bash
./gradlew :lwjgl3:jar          # cross-platform JAR
./gradlew :lwjgl3:jarWin       # Windows-only
./gradlew :lwjgl3:jarMac       # macOS-only
./gradlew :lwjgl3:jarLinux     # Linux-only
```
Output is placed in `lwjgl3/build/libs/`.

## Key versions

| Library  | Version |
|----------|---------|
| Kotlin   | 2.3.0   |
| LibGDX   | 1.14.0  |
| KTX      | 1.13.1-rc1 |
| Ashley   | 1.7.4   |
| Supabase | 3.5.0   |
| Ktor     | 3.4.2   |

## Project setup notes

- Player identity (`myPlayerId`, `myPlayerName`) is persisted in LibGDX `Preferences` so it survives app restarts.
- The Supabase project URL and public anon key are embedded in `SupabaseClient.kt`. No extra configuration file is needed to connect.
- Game assets (textures, sprites, backgrounds) live in `android/assets/` and are symlinked/shared with the desktop module via the asset path configuration in `lwjgl3/build.gradle.kts`.
- Map constants (dimensions, city count, production values) are centralised in `config/GameConstants.kt`. Rendering layer order is in `config/ZIndex.kt`.
