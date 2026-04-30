#### HOS Timeline

HOS Timeline is a historical timeline and event management system designed for Mohist `1.20.1` Minecraft servers. It allows server owners to create a configurable in-game historical progression system with date-based events, automatic time advancement, conditional triggers, flags, broadcasts, console commands and persistent event logs.

The project is intended for roleplay, history-themed, civilization, kingdom, war and scenario-based Minecraft servers where server progression needs to be controlled through eras, dates or scripted events.

#### Features

- Designed for Mohist `1.20.1`
- Configurable timeline date system
- Automatic time advancement
- Manual date control
- Date-based event triggering
- Conditional event support
- Flag-based progression logic
- Broadcast event handler
- Console command event handler
- Set-flag event handler
- YAML-based configuration
- Persistent storage support
- Event logging
- Reload command
- Public API and integration registry
- Expandable event handler structure

#### Commands

- `/timeline show` — Shows the current timeline date.
- `/timeline set <date>` — Sets the current timeline date.
- `/timeline advance <amount>` — Advances the timeline manually.
- `/timeline pause` — Pauses automatic timeline progression.
- `/timeline resume` — Resumes automatic timeline progression.
- `/timeline reload` — Reloads configuration files.
- `/timeline log` — Displays recent timeline event logs.

#### Configuration

HOS Timeline uses YAML configuration files.

Main files:

- `config.yml`
- `timeline_events.yml`

`config.yml` contains the main timeline settings such as the starting date and automatic time advancement options.

`timeline_events.yml` contains the list of custom events that should be triggered on specific timeline dates or under certain conditions.

#### Event System

Events can be configured to run when a specific date is reached. Each event can perform different actions, such as broadcasting a message, executing a console command or setting a flag.

This makes it possible to build structured historical progression, scripted server milestones, era changes and dynamic roleplay events.

#### Flag System

Flags allow events to depend on previous events or server states. An event can set a flag, and another event can require that flag before it can run. This allows server owners to build conditional and branching timeline logic.

#### Use Cases

HOS Timeline can be used for:

- Historical Minecraft servers
- Roleplay servers
- Civilization servers
- Kingdom and war servers
- Era-based progression systems
- Scenario-based survival servers
- Modded Mohist servers
- Server-wide scripted event systems

#### Compatibility

This project is built for Mohist `1.20.1`. Compatibility with other server platforms is not guaranteed.


#### Technical Overview

HOS Timeline is structured around a modular event-driven architecture.

The main plugin class initializes configuration, storage, the timeline engine, command handlers and event handlers. The timeline engine maintains the current date and coordinates automatic advancement. The event scheduler checks configured timeline events and triggers them when their date and conditions match.

Event handlers define what happens when an event is fired. The included handlers support broadcasting messages, executing console commands and setting internal flags. The flag system allows events to depend on previous timeline states, making it possible to create more complex server progression logic.

The storage layer uses YAML-based persistence to save timeline data, flags and event logs. The API package exposes core timeline functionality and Bukkit-style events so other plugins can listen to timeline changes or integrate with the system.
