# VTTale

**A system-agnostic Virtual Tabletop (VTT) core designed to run "headless" inside game engines.**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-Multi--module-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-LGPL--3.0-blue)](COPYING.LESSER)

---

## Overview

VTTale is a **headless VTT engine** that brings tabletop RPG functionality (dice rolling, character management, game systems) into existing game engines like **Hytale**. Unlike traditional VTTs with their own UI, VTTale runs as a plugin inside the host game, using its existing chat and command systems.

### Key Features

- **Platform Agnostic** — Designed to work across multiple game engines via Adapters
- **Modular Architecture** — Extend functionality without modifying core code
- **Multi-System Support** — Support for D&D 5e, Pathfinder, and other game systems via modules
- **Event-Driven** — Type-safe event bus for inter-module communication
- **Plugin-Friendly** — Third-party developers can create their own extensions

### Inspired by FoundryVTT

VTTale follows architectural patterns similar to [FoundryVTT](https://foundryvtt.com/):

| Aspect               | FoundryVTT                     | VTTale                                        |
| -------------------- | ------------------------------ | --------------------------------------------- |
| **Language**         | JavaScript                     | Java                                          |
| **Event System**     | `Hooks.on()` / `Hooks.call()`  | `EventBus.subscribe()` / `EventBus.publish()` |
| **Module Discovery** | JSON manifest + script loading | Java SPI (`ServiceLoader`)                    |
| **UI**               | Full web-based UI              | Headless (uses host game's UI)                |
| **Extensibility**    | Modules & Systems              | Modules, Game Systems & Platform Adapters     |

---

## Architecture

VTTale uses a **Micro-kernel / Plug-in Based / Hexagonal** architecture.

### Module Structure

```
VTTale/
├── api/                    # SDK - Interfaces & Data Classes
├── kernel/                 # Core - Module Lifecycle & Event Bus
├── module/                 # Built-in Features
│   ├── chat/               # Chat messaging module
│   └── diceroll/           # Dice rolling module
├── gamesystem/             # Game System Rulesets
│   └── dnd5e/              # D&D 5th Edition
└── platform/               # Platform Adapters
    └── hytale/             # Hytale game engine adapter
```

### Dependency Flow

The strictly enforced dependency flow is:

```
platform → api ← kernel
              ↑
         module/gamesystem
```

| Module         | Description                                         | Dependencies                                  |
| -------------- | --------------------------------------------------- | --------------------------------------------- |
| **api**        | The "Contract" — Interfaces and Data Classes        | None                                          |
| **kernel**     | The "Runner" — Manages Module Lifecycle & Event Bus | `api`                                         |
| **module**     | Core features (chat, dice rolling)                  | `api`                                         |
| **gamesystem** | Specific game systems (D&D 5e, Pathfinder)          | `api`                                         |
| **platform**   | Game engine bridges (Hytale, Minecraft)             | `api`, `kernel` (runtime), `module` (runtime) |

---

## Tech Stack

- **Language:** Java 21
- **Build System:** Gradle (Kotlin DSL, Multi-module)
- **Architecture:** Micro-kernel / Plug-in Based / Hexagonal
- **Distribution:** Fat JAR (All-in-one platform plugin)

---

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Gradle 8.x** (or use the included wrapper)

### Building the Project

```bash
# Clone the repository
git clone https://github.com/your-username/VTTale.git
cd VTTale

# Build all modules
./gradlew build

# Build the Hytale platform plugin
./gradlew :platform:hytale:shadowJar
```

### Running the Hytale Server

```bash
./gradlew :platform:hytale:runServer
```

---

## Distribution

### Fat JAR

The project is built as a **Single Fat JAR** for each platform (e.g., `VTTale.jar`). This JAR encapsulates:

- The VTT Kernel
- The Platform Adapter (Bootstrap)
- All built-in Modules and Game Systems

---

## Extending VTTale

### Creating a Third-Party Module

Native game engine developers can extend VTTale by creating their own plugins:

1. Add `api` as a compile-time dependency
2. Implement the `Module` interface
3. Register your module via the `VTTale` API at runtime
4. Interact with the global `EventBus` and `Registry`

### Example: Custom Module

```java
public class MyCustomModule implements Module {

    @Override
    public String getName() {
        return "my-custom-module";
    }

    @Override
    public void onEnable(Kernel kernel) {
        // Subscribe to events
        kernel.getEventBus().subscribe(CommandExecutedEvent.class, (event, ctx) -> {
            // Handle command execution
        });

        // Register custom commands
        kernel.getCommandRegistry().registerCommand(
            "mycommand",
            CommandOptions.builder()
                .description("My custom command")
                .build()
        );
    }

    @Override
    public void onDisable(Kernel kernel) {
        // Cleanup
    }
}
```

---

## Key Design Patterns

### Type-Safe Event Bus

Communication happens via a typed Event Bus:

```java
// Subscribe to events
bus.subscribe(MyEvent.class, (evt, ctx) -> {
    // Handle event
});

// Publish events
bus.publish(new MyEvent(), context);
```

### Dynamic Command Registry

Commands are registered dynamically at runtime:

```java
kernel.getCommandRegistry().registerCommand(
    "roll",
    CommandOptions.builder()
        .description("Roll dice")
        .playerOnly(true)
        .build()
);
```

### Event Context & Routing

Every event travels with an `EventContext` containing the `SenderID` (UUID) and `SourceAdapter`, allowing responses to be routed back to the correct user in the game world.

---

## Project Structure

```
VTTale/
├── api/
│   └── src/main/java/dev/giopalma/vttale/api/
│       ├── Kernel.java              # Core kernel interface
│       ├── KernelProvider.java      # SPI provider interface
│       ├── VTTale.java              # Main API entry point
│       ├── command/
│       │   ├── CommandOptions.java  # Command configuration
│       │   └── CommandRegistry.java # Command registration interface
│       ├── events/
│       │   ├── Event.java           # Base event interface
│       │   ├── EventBus.java        # Event bus interface
│       │   ├── EventContext.java    # Event routing context
│       │   └── ...
│       └── module/
│           ├── Module.java          # Module interface
│           └── ModuleRegistry.java  # Module registry interface
├── kernel/
│   └── src/main/java/dev/giopalma/vttale/kernel/
│       ├── VTTaleKernel.java        # Kernel implementation
│       ├── VTTaleKernelProvider.java
│       ├── command/
│       │   └── SimpleCommandRegistry.java
│       ├── events/
│       │   └── SimpleEventBus.java
│       └── module/
│           └── SimpleModuleRegistry.java
├── module/
│   └── src/main/java/dev/giopalma/vttale/module/
│       ├── chat/
│       │   ├── ChatModule.java
│       │   ├── PlatformBroadcastEvent.java
│       │   └── SendMessageEvent.java
│       └── diceroll/
│           └── DiceRollModule.java
├── gamesystem/
│   └── src/main/java/dev/giopalma/vttale/gamesystem/
│       └── dnd5e/
│           └── DND5EGameSystem.java
└── platform/
    └── hytale/
        └── src/main/java/dev/giopalma/vttale/platform/hytale/
            ├── Command.java
            ├── HytaleAdapter.java
            └── VTTaleHytalePlugin.java
```

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## License

This project is licensed under the [GNU Lesser General Public License v3.0](COPYING.LESSER).

---

## Acknowledgments

- [FoundryVTT](https://foundryvtt.com/) — For architectural inspiration
- [Hytale](https://hytale.com/) — For the initial platform target
