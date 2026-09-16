# Required device acceptance — not yet executed

Record APK commit, Android version, device, RAM, renderer, FPS and outcome.

1. Install signed debug APK on API16 device/emulator. Confirm fresh install, account signup/login/restart, no VerifyError/NoSuchMethodError.
2. Offline with airplane mode: move, rotate camera, jump, climb up/down and exit onto platform. Confirm proper R6 silhouette and readable UI.
3. Enable one bot after warning: watch complete ladder traversal, survivor list, death/respawn and at least two full rounds.
4. Chat Russian: привет; иди за мной; лезь наверх. Repeat English. Check cooldown and bounded memory.
5. Phone A host / phone B join same router without WAN. Discover, connect, move simultaneously, chat, disconnect/rejoin; duplicate names rejected.
6. Same test with PC host. Test direct IP with UDP blocked, then router isolation (clear failure rather than hang).
7. Lock/restore client, leave/re-enter repeatedly, disconnect Wi-Fi; verify no duplicate actors and no stale input.
8. Lenovo A319-class: 20 FPS, studs off, 1 then 2 bots. Measure 10-minute PSS/RSS, median/p95 frame time, CPU and battery. Increase bot count only manually.
9. No claim of 30/20 FPS until these measurements exist. No Tiny LLM tests until a real runtime is present.

CI currently covers pure-Java movement/navigation/flood and loopback networking plus Android compilation/API lint/minSdk/signature. It cannot certify a real router or old GPU.
