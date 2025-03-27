
Save New Duplicate & Edit Just Text
# networking changelog

## data component types

- default item components changed:
    - added default item break sound
    - added default tooltip display

### can_place_on + can_break

- block predicates now have a data component matcher (new structure)

### unbreakable

- no longer has contents, is either present or not

### hide_additional_tooltip + hide_tooltip

- is gone, replaced with tooltip_display

### tooltip_display

- new
- bool: whether to hide the tooltip or not
- list of data component types: which should be hidden

### weapon

- new
- varint: item damage per attack
- float: disable blocking for seconds

### blocks_attacks

- new
- float: block delay seconds
- float: disable cooldown scale
- list of damage reduction: damage reductions
- list of item damage functions: item damage
- optional damage type tag key: bypassed by
- optional sound event holder: block sound
- optional sound event holder: disable sound

#### damage reduction

- float: horizontal blocking angle
- optional holder set of damage type: type
- float: base
- float: factor

#### item damage function

- float: threshold
- float: base
- float: factor

### potion_duration_scale

- new
- single float

### provides_trim_material

- new
- single trim material holder

### provides_banner_patterns

- new
- single banner pattern tag key

### break_sound

- new
- single sound event holder

### villager/variant

- new
- single villager type holder

### wolf/variant

- new
- single wolf variant type holder

### wolf/sound_variant

- new
- single wolf sound variant type holder

### wolf/collar

- new
- single dye color enum

### fox/variant

- new
- single fox variant enum

### salmon/size

- new
- single salmon variant enum

### parrot/variant

- new
- single parrot variant enum

### tropical_fish/pattern

- new
- single tropical fish pattern enum

### tropical_fish/base_color

- new
- single dye color enum

### tropical_fish/pattern_color

- new
- single dye color enum

### mooshroom/variant

- new
- single mooshroom cow variant enum

### rabbit/variant

- new
- single rabbit variant enum

### pig/variant

- new
- single pig variant holder

### cow/variant

- new
- single cow variant holder

### chicken/variant

- new
- single chicken variant holder (can be direct)

### frog/variant

- new
- single frog variant holder

### horse/variant

- new
- single horse variant enum

### painting/variant

- new
- single painting variant holder

### llama/variant

- new
- single llama variant enum

### axolotl/variant

- new
- single axolotl variant enum

### cat/variant

- new
- single cat variant holder

### cat/collar

- new
- single dye color enum

### sheep/color

- new
- single dye color enum

### shulker/color

- new
- single dye color enum

## command argument types

### resource_selector

- new
- single resource key as content

## item stacks

- there now is a codec for "untrusted" item stack data (used in serverbound creative slot packet); this just prefixes each data component entry with the size of it

## particle types

### tinted_leaves

- new
- same contents as entity_effect particle type

### firefly

- new
- no contents

## registry synchronization

+ wolf_sound_variant
+ pig_variant
+ frog_variant
+ cat_Variant
+ cow_variant
+ chicken_variant
+ test_environment
+ test_instance

## nbt

- snbt now accepts "mixed arrays" and converts them in some strange pattern; irrelevant to packetevents

## chat components

- serialization click events and hover events have been refactored
- clickEvent is now called click_event
- hoverEvent is now called hover_event

## packets

### clientbound add experience orb packet

- removed, finally!
- this is now sent as entity data

### clientbound level chunk packet data

- heightmaps are no longer serialized as nbt and instead serialized as a map of "heightmap type enum" to "long array"

### clientbound update advancements packet

- bool: show advancements (last)

# Hard to implement

### clientbound test instance block status packet

- new
- component: status
- optional vec3i: size

### serverbound container click packet

- slots and carried item is no longer a raw itemstack, but instead a "hashed stack" (new structure)

### serverbound set creative mode slot packet

- the item stack is now written as an "untrusted" item stack, see above for more info

### serverbound test instance block action packet

- new
- block pos: position
- action enum: action
- test instance block entity data: data

#### test instance block entity data

- new structure
- optional test instance registry key: test
- vec3i: size
- rotation: rotation
- bool: ignore entities
- test instance block entity status enum: status
- optional component: error message

## TODO

continue @ AdvancementCommands