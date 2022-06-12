# Jerraria
Terraria clone in Java

![Latest Progress](https://cdn.discordapp.com/attachments/907782618899169313/985308757112262696/unknown.png)

## Open Source Modules
Parts of the Jerraria codebase are opensource and can be freely used. These include:

	- jerraria-access module
	- jerraria-common module
	- jerraria-rendering module

They are all licenced under LGPLv3 in `OSS_MODULE_LICENCE`

## Common
 - [x] multithreaded ticking
 - [x] infinite worlds
 - [x] dimensions
 - [x] nbt alternative
 - [ ] denser chunk files
 - [ ] region files
 - [x] entity ticking
 - [ ] entity collision
 - [ ] block collisions
 - [x] Tile, TileVariant & TileData
 - [x] scheduled ticks
 - [x] TileData ticking
 - [x] Item
 - [ ] networking
   - [x] connection
   - [ ] authentication
   - [ ] good packet api
 - [ ] players
 - [ ] content
   - [ ] what kind of game are we actually making
 - [x] resource/data-driving system
 - [x] registry
 - [ ] worldgen
   - [ ] base worldgen
   - [ ] biomes
   - [ ] hell?
   - [ ] decorations (ores, trees)
   - [ ] structures

## Client
 - [x] rendering api
   - [x] shader loading
   - [x] texture loading
   - [x] atlas stitching
   - [x] animated textures
   - [x] type-safe java api
   - [x] atomic counters
   - [x] SSBOs
   - [x] UBOs
   - [x] instanced rendering
     - [x] struct copying (copy whole structs of data at once)
   - [x] order independent translucency
     - [x] linked list (OpenGL 4.3+)
     - [x] weighted (OpenGL 4.0+)
     - [x] dual pass weighted (OpenGL 3.3+)
 - [x] world rendering
 - [ ] gui api
 - [x] loading screen
 - [ ] main menu
   - [ ] server selector
   - [ ] world selector
   - [ ] settings menu
