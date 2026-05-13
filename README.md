# killsayApiSdk
写readme的傻逼gpt是洋人
`killsayApiSdk` is a stripped Fabric client module extracted from `Killsay Reborn`.

It keeps only:

- detection
- tracking
- public API

It removes:

- GUI
- say sending
- wanxin
- gufa
- auto reply
- auto report
- HUD

Core sources:

- `src/main/java/mojang/minecraft/uuidget/ClientInitializer.java`
- `src/main/java/mojang/minecraft/uuidget/KillsayEvents.java`
- `src/main/java/mojang/minecraft/uuidget/ClientOptions.java`
- `src/main/java/mojang/minecraft/uuidget/HealthTracker.java`

Build artifact:

- `killsayApiSdk`

## License Restrictions

This project is not MIT anymore.

The repository is distributed under `KillsayApiSdk Restricted License v1.0`.

Main restrictions:

- commercial use is forbidden
- adding new functionality into this project is forbidden
- restoring removed modules is forbidden
- the only allowed extension path is using the existing public API from external code

See `LICENSE` for the full text.
