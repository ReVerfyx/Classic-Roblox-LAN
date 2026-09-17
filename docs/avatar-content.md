# Local original R6 content

Settings → Import R6 from local OBB → select the 2017
`main.1.com.roblox.client.obb` from the user's own device.
The wardrobe opens after validation. Subsequent launches require no network.

Reference pack: https://github.com/fomeinsaciavel/Pekora-Unofficial-Clients/tree/main/2017/assets

The importer copies only six `content/avatar/{heads,meshes}/*.mesh` files and
`content/textures/face.png` into a versioned private data directory. It reads
mesh 2.00 positions, normals and indexed triangles; it never executes scripts.
Imports are validated before replacing the saved pack, and temporary OBB files
are removed. No source assets or original binaries are distributed in this repo.

The original mesh and face render in the wardrobe. Body color, local outfit tint,
hat and basic animation controls remain available. This does not yet port the
binary AvatarEditor.rbxl room, catalog accessories, texture clothing, animation
packages, or the in-game avatar renderer. The OBB contains editor Lua and a binary
place, but running those requires Roblox services our engine does not implement.

Validation: tested parsing all six meshes in the supplied 2017 archive; unit
checks reject invalid indices and truncated layouts. Android device visual
validation remains necessary; passing compilation does not establish visual parity.
