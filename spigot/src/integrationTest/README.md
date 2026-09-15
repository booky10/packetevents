# Experimental Minecraft 26.3 support

Target: protocol 777, Paper 26.3 build 5 (`ace932e`), Java 25.
Based on [upstream PR #1582](https://github.com/retrooper/packetevents/pull/1582)
at `16124da5bd8c165ce82eaaf3f5184aebd0e7c60f`.

This contribution adds packet IDs and changed codecs, generated block/item
registries and defaults, tags, particles, item components, movement paths, recipe
holder sets, biome modifiers, and trim palettes. Registry data comes from Minecraft
26.3's server reports and the PacketEvents data generators. Protocol changes are
gated by version; the existing older-version tests remain enabled.

## Run

From the repository root, build with the project's existing Java toolchains available:

```powershell
.\gradlew.bat :api:test :spigot:shadowJar --console=plain
```

Use `./gradlew` on Linux/macOS. The integration scripts require Java 25 (`java`,
`javac`, and `jar`) and Node.js 18+ on PATH; they add no project dependencies.

Prepare a disposable server under `run/paper/26.3`:

1. Download [Paper 26.3 build 5](https://fill.papermc.io/v3/projects/paper/versions/26.3/builds/5)
   and save its server JAR as `paper-26.3-5.jar` in that directory.
2. Run `java -jar paper-26.3-5.jar --nogui` from that directory once to extract
   `libraries` and `versions/26.3/paper-26.3.jar` and create `server.properties`.
   If it starts fully, stop it before continuing.
3. Read the Minecraft EULA and set `eula=true` in that directory's `eula.txt` if
   you agree. Keep this test server's `plugins` directory free of other plugins.

The runner configures this server for localhost port 25633, offline login, and
view/simulation distance 2. It modifies the disposable world, spawns entities,
and triggers an explosion. Do not place an existing server or world there.

From the repository root:

```powershell
node spigot/src/integrationTest/compile-26.3.cjs
node spigot/src/integrationTest/run-26.3.cjs
```

The runner starts Paper, connects a minimal protocol client, checks login and play,
then shuts down the server. It exits unsuccessfully for probe failures, server
errors, timeouts, or premature disconnection. Output is in `build/probe-server.log`.
The probe is a separate test plugin; it is not included in the PacketEvents JAR.
The compile script accepts an alternative PacketEvents JAR path as its first
argument. It reads the default artifact version from `gradle.properties`.

`protocol-777.json` contains the packet IDs used by the minimal client, extracted
from Minecraft 26.3's `net.minecraft.data.Main --reports` output. The probe writes
its Minecraft-encoded packet fixtures to
`run/paper/26.3/plugins/PacketEvents26_3Probe/packet-fixtures.tsv`; the checked-in
copy is `api/src/test/resources/26.3-packets.tsv`.

## Coverage

- 83 API tests, including the retained older-version checks and new regressions.
- All 35,723 block-state IDs and 1,658 default item conversions.
- 3,890 distinct default component values decoded and re-encoded through both
  Minecraft and PacketEvents; custom sign text and decorated-pot templates.
- 12 byte-for-byte Minecraft packet fixtures: particle randomization modes,
  transient blocks, both swing hands, linear/stepped movement, and both recipe
  holder-set forms. Fixtures are retained under `api/src/test/resources`.
- Live configuration registries, login, chunks, recipes, commands, inventory,
  teleports, movement, particles, new entity metadata, and explosions.

This is an experimental build tested against the specified Paper alpha. The
minimal client does not render the world. Full graphical-client play, proxy and
ViaVersion combinations, Folia, and other plugins' behavior are not certified.
The new `block_transformer` and `provides_pottery_pattern` components currently
expose numeric registry IDs; their public API representation needs review.
