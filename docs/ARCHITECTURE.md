# Architecture and fidelity contract

## Era: 2012

Visual target: gray rectangular bevel/gradient panels, small sans-serif text, chat upper left, player list upper right, tool area at bottom, health lower right. Current UI is an approximation, not an extracted historical client. Login follows the supplied early mobile reference: portrait layout, blue sunburst, green hill, skyline, red outlined white wordmark and white input fields. These are original Canvas/vector elements. Gameplay uses a translucent analog thumbstick left, round arrow jump button right, and swipe camera with independent pointer ownership. Android settings use standard Holo dialogs. No modern cards, voxel terrain or Minecraft mining/building mechanics.

Reference research:
- Historical November 2012 chat/backpack footage: https://www.youtube.com/watch?v=hnTtlTNnURY
- 2012 UI reconstruction discussion: https://devforum.roblox.com/t/game-ui-feedback/2971900 (full visual page unavailable to the development environment).
- Android graphics API support: https://developer.android.com/develop/ui/views/graphics/opengl/about-opengl
- Build-tool versions: https://developer.android.com/build/releases/agp-8-7-0-release-notes

Historical screenshot comparison is still required; the above is a design target rather than a claim of pixel-exact reconstruction.

## R6

Stud units, center at torso. Torso 2×2×1; arms 1×2×1; legs 1×2×1; nominal Head part 2×1×1. Current rendered head is a cylinder 1.25 wide, 1 tall. Torso center is 3 studs above feet; head center +1.5; leg centers -2. Shoulder articulation is procedural. Death currently tilts the whole rig, not detached limbs. Movement speed 16 studs/sec, gravity 196.2, jump velocity 50; these are initial tuning values, not proof of matching the original engine.

## Stack / API16

No engine SDK, AndroidX, Kotlin runtime or JNI. Standard Java shared simulation, android.app.Activity, GLSurfaceView and javax.microedition GL10. OpenGL ES 1.1 chosen for older fixed-function hardware; Android official documentation confirms support predates API16. JDK17/Gradle8.9 are build-host requirements only; generated Java target is 8 with Android D8 dex conversion. Full API16 device validation is pending.

```
Classic-Roblox-LAN/
  android/src/main/AndroidManifest.xml
  android/src/main/java/lan/classic/android/
    MainActivity.java  # accounts, menus, touch, host/join lifecycle
    Accounts.java      # device-local salted password verification
    ClassicLoginArt.java # original vector login background and wordmark
    TouchControl.java   # analog joystick and round jump control
    GameView.java      # GLES renderer
  core/src/main/java/lan/classic/
    Actor.java
    Part.java
    World.java
    Navigation.java
    ClassicAI.java
    LLMBackend.java
    Net.java
  server/src/main/java/lan/classic/server/Main.java
  tests/CoreTest.java
  docs/
  .github/workflows/build.yml
```

## Data and networking

Part has Position, Rotation, Size, Color, Material, Anchored, CanCollide, Transparency, Shape and surface flags. Only axis-aligned blocks/static collisions are implemented in this milestone. Map geometry currently comes from the versioned core rather than arbitrary remote data; both peers must use exactly the same build.

TCP 53640: client sends magic 0x434c414e, protocol version 2, bounded username and language. Server replies magic and actor ID. Each request carries opcode 1 (input) or 2 (reset self), two normalized movement axes, jump flag, and bounded UTF-8 chat. Server replies a snapshot with phase, water, actors and recent chat. Limits: 20 actors, 8 chat lines, 160 chat characters, bounded length-prefix allocations, timeout 5s. No Java object deserialization. No account password travels over LAN. This is trusted-LAN identity, not cross-device password authentication.

UDP 53641: CLASSIC_LAN_DISCOVER_2 → CLASSIC_LAN_2|port|name|map|count|capacity. Broadcasts sent to interface broadcasts and global broadcast; direct IP fallback. Android holds a MulticastLock during LAN use. No external rendezvous or internet API.

## Simulation and bots

Host advances at 30Hz, render independently at 20/30/60, client polls snapshots at up to 20Hz. Navigation uses a directed graph and shortest paths. Ladder edges use the same collision/proximity climb mechanic as a human. Graph route is cached, not calculated every frame. Current graph contains walk → ladder → platform; other edge enum values are future extensions.

CLASSIC intent detection updates goals; social code never controls frame-by-frame motion. Follow works on approximately the same elevation; the authored ladder route is used for height seeking. Flood triggers high-place behavior. Memory is bounded to six messages, server chat to eight. One shared ClassicAI service for all actors. Chat cooldown prevents multiple bots replying to every message.

Tiny LLM is not implemented. Before adding a backend, measure APK+model size, process startup PSS/RSS, model-load peak, steady inference memory and tokens/sec on ARMv7. A backend may load only when explicitly selected, with one shared queue and one model. An unloaded/unavailable backend uses CLASSIC. Do not advertise an unmeasured model as 512MB-compatible.

## Map roadmap

Classic Baseplate is an original procedural test level. Crossroads, Happy Home in Robloxia, Chaos Canyon, Glass Houses, Rocket Arena, Sword Fight on the Heights, Work at a Pizza Place and Natural Disaster Survival are not bundled. Implement a bounded RBXLX/portable-parts importer for user-supplied authorized assets next; do not silently substitute invented maps under those names. Roblox scripts require separate behavior reimplementation, not arbitrary evaluation.

Do not start additional maps until API16 install/render and two-device Wi-Fi tests pass.

## Menu reference revision (0.1.1)

The supplied green mobile Home and translucent escape menu are used as explicit user-selected references. The visual contract is now intentionally mixed: 2012-style world/R6, early mobile login, and the later supplied mobile Home/escape menu. It is not claimed to be one pixel-exact 2012 client.

Research: https://www.webdesignmuseum.org/gallery/roblox-2015 (archived website, not a mobile screenshot); https://github.com/bloxstraplabs/bloxstrap/issues/2101 (primary project discussion identifying the 2015 escape menu). Some historical mobile pages were blocked. The supplied screenshots are the decisive visual source. No old APK was executed or imported.

ClassicHome.java provides native green-header navigation, persisted recents/favorites/local friends and last-session chat. ClassicPauseMenu.java shows session players, profile details, local Add Friend, live FPS/studs settings, help, resume and confirmed leave/reset. Reset uses protocol v2 and can reset only the requesting peer. Opening a menu stops local input but does not pause the host simulation. Friends are local saved names, not authenticated global accounts; no fake online counts or messaging backend.
