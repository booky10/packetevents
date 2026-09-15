import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.*;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.display.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/** Runs only in the isolated Paper 26.3 integration server. */
public final class PacketEvents26_3Probe extends JavaPlugin {
    private final AtomicInteger failures = new AtomicInteger();
    private final AtomicInteger livePackets = new AtomicInteger();
    private final Set<String> liveTypes = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final List<String> fixtures = new ArrayList<>();
    private User testUser;

    @Override
    public void onEnable() {
        testUser = new User(null, ConnectionState.PLAY, ClientVersion.V_26_3,
                new UserProfile(new UUID(0, 1), "CodecProbe"));
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract() {
            @Override
            public void onPacketSend(PacketSendEvent event) {
                inspectLive(event, PacketSendEvent.class);
            }

            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                inspectLive(event, PacketReceiveEvent.class);
            }
        });
        Bukkit.getScheduler().runTask(this, () -> {
            check("server version", () -> require(PacketEvents.getAPI().getServerManager().getVersion()
                    == ServerVersion.V_26_3, "Incorrect server version"));
            check("all block states", this::blocks);
            check("all items", this::items);
            check("all default item components", this::components);
            check("packet codecs", this::packets);
            getLogger().info("PROBE_READY failures=" + failures.get());
        });
    }

    private void inspectLive(ProtocolPacketEvent event, Class<?> eventClass) {
        Class<? extends PacketWrapper<?>> wrapperClass = event.getPacketType().getWrapperClass();
        if (wrapperClass == null) return;
        Object previous = event.getLastUsedWrapper();
        ByteBuf source = (ByteBuf) event.getByteBuf();
        int index = source.readerIndex();
        try {
            event.setLastUsedWrapper(null);
            wrapperClass.getConstructor(eventClass).newInstance(event);
            require(!source.isReadable(), event.getPacketType() + " left " + source.readableBytes() + " bytes");
            livePackets.incrementAndGet();
            liveTypes.add(event.getPacketType().getName());
        } catch (Throwable error) {
            fail("live " + event.getPacketType(), error);
        } finally {
            source.readerIndex(index);
            event.setLastUsedWrapper((PacketWrapper<?>) previous);
        }
    }

    private void blocks() {
        int count = 0;
        for (var state : Block.BLOCK_STATE_REGISTRY) {
            int id = Block.getId(state);
            var actual = WrappedBlockState.getByGlobalId(ClientVersion.V_26_3, id, false);
            String key = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            require(actual.getType().getName().equals(key.substring("minecraft:".length())), "Block " + id + ": " + key);
            require(actual.getGlobalId() == id, "Block state " + id);
            count++;
        }
        getLogger().info("BLOCK_STATES_OK " + count);
    }

    private void items() {
        int count = 0;
        for (var item : BuiltInRegistries.ITEM) {
            var original = CraftItemStack.asBukkitCopy(item.getDefaultInstance());
            var converted = SpigotConversionUtil.fromBukkitItemStack(original);
            var restored = SpigotConversionUtil.toBukkitItemStack(converted);
            require(original.equals(restored), "Item " + BuiltInRegistries.ITEM.getKey(item));
            count++;
        }
        getLogger().info("ITEMS_OK " + count);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void components() {
        Set<String> seen = new HashSet<>();
        int count = 0;
        for (var item : BuiltInRegistries.ITEM) {
            for (TypedDataComponent<?> component : item.components()) {
                var type = component.type();
                if (type.isTransient()) continue;
                String name = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type).toString();
                RegistryFriendlyByteBuf input = buffer();
                RegistryFriendlyByteBuf output = buffer();
                try {
                    ((StreamCodec) type.streamCodec()).encode(input, component.value());
                    String key = name + ':' + ByteBufUtil.hexDump(input);
                    if (!seen.add(key)) continue;
                    var peType = (com.github.retrooper.packetevents.protocol.component.ComponentType) ComponentTypes.getByName(name);
                    require(peType != null, "Missing component " + name);
                    Object decoded = peType.read(PacketWrapper.createUniversalPacketWrapper(input));
                    require(!input.isReadable(), "Component remainder " + name + " on " + BuiltInRegistries.ITEM.getKey(item));
                    peType.write(PacketWrapper.createUniversalPacketWrapper(output), decoded);
                    Object restored = ((StreamCodec) type.streamCodec()).decode(output);
                    require(!output.isReadable(), "Component output remainder " + name);
                    require(component.value().equals(restored), "Component mismatch " + name + " on " + BuiltInRegistries.ITEM.getKey(item));
                    count++;
                } finally {
                    input.release();
                    output.release();
                }
            }
        }
        getLogger().info("COMPONENT_VALUES_OK " + count);
        List<Component> messages = List.of(Component.literal("one"), Component.literal("two"), Component.empty(), Component.literal("four"));
        List<Component> filtered = List.of(Component.literal("filtered"), Component.empty(), Component.empty(), Component.empty());
        for (TypedDataComponent<?> component : List.<TypedDataComponent<?>>of(
                new TypedDataComponent<>(DataComponents.SIGN_TEXT_FRONT, new SignText(messages, messages, DyeColor.RED, true)),
                new TypedDataComponent<>(DataComponents.SIGN_TEXT_BACK, new SignText(messages, filtered, DyeColor.BLUE, false)),
                new TypedDataComponent<>(DataComponents.POT_DECORATIONS, new PotDecorations(
                        Optional.of(new ItemStackTemplate(Items.ANGLER_POTTERY_SHERD)), Optional.empty(),
                        Optional.of(new ItemStackTemplate(Items.BRICK)), Optional.empty())))) {
            var type = component.type();
            String name = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type).toString();
            RegistryFriendlyByteBuf input = buffer();
            RegistryFriendlyByteBuf output = buffer();
            try {
                ((StreamCodec) type.streamCodec()).encode(input, component.value());
                var peType = (com.github.retrooper.packetevents.protocol.component.ComponentType) ComponentTypes.getByName(name);
                var decoded = peType.read(PacketWrapper.createUniversalPacketWrapper(input));
                require(!input.isReadable(), name + " trailing bytes");
                peType.write(PacketWrapper.createUniversalPacketWrapper(output), decoded);
                Object restored = ((StreamCodec) type.streamCodec()).decode(output);
                require(component.value().equals(restored), name + " changed");
                require(!output.isReadable(), name + " trailing output");
            } finally {
                input.release();
                output.release();
            }
        }
        getLogger().info("CUSTOM_COMPONENTS_OK 3");
    }

    private void packets() throws Exception {
        for (var randomization : ClientboundLevelParticlesPacket.RandomizationType.values()) {
            packet("particle_" + randomization, ClientboundLevelParticlesPacket.STREAM_CODEC,
                    new ClientboundLevelParticlesPacket(ParticleTypes.FLAME, true, false, 1.25, -2.5, 3.75,
                            .1f, .2f, .3f, .4f, .5f, .6f, 300, randomization), PacketType.Play.Server.PARTICLE);
        }
        packet("transient_block", ClientboundAddTransientBlockPacket.STREAM_CODEC,
                new ClientboundAddTransientBlockPacket(new BlockPos(-100, 65, 300), Blocks.POPLAR_STAIRS.defaultBlockState()),
                PacketType.Play.Server.ADD_TRANSIENT_BLOCK);
        for (var hand : InteractionHand.values()) {
            packet("swing_" + hand, ClientboundSwingAnimationPacket.STREAM_CODEC,
                    new ClientboundSwingAnimationPacket(321, hand, SwingAnimation.DEFAULT), PacketType.Play.Server.SWING_ANIMATION);
        }
        var linear = new VecDelta.Linear((short) -234, (short) 765, (short) 23);
        var stepped = new VecDelta.Stepped(List.of(
                new VecDelta.Stepped.DeltaStep((short) 1000, (short) -2000, (short) 3000, 3),
                new VecDelta.Stepped.DeltaStep((short) -200, (short) 100, (short) -50, 5)));
        for (var delta : List.of(linear, stepped)) {
            packet("move_" + delta.stepCount(), ClientboundMoveEntityPacket.Pos.STREAM_CODEC,
                    new ClientboundMoveEntityPacket.Pos(321, delta, true), PacketType.Play.Server.ENTITY_RELATIVE_MOVE);
            packet("move_rotate_" + delta.stepCount(), ClientboundMoveEntityPacket.PosRot.STREAM_CODEC,
                    new ClientboundMoveEntityPacket.PosRot(321, delta, (byte) 37, (byte) -25, false),
                    PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION);
        }
        var named = BuiltInRegistries.ITEM.getOrThrow(net.minecraft.tags.TagKey.create(Registries.ITEM,
                net.minecraft.resources.Identifier.withDefaultNamespace("planks")));
        var direct = HolderSet.direct(Items.POPLAR_PLANKS.builtInRegistryHolder(), Items.OAK_PLANKS.builtInRegistryHolder());
        for (var items : List.of(named, direct)) {
            var display = new ShapedCraftingRecipeDisplay(2, 2,
                    Collections.nCopies(4, new SlotDisplay.TagSlotDisplay(items)),
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.CRAFTING_TABLE)),
                    new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE));
            var entry = new RecipeDisplayEntry(new RecipeDisplayId(339), display, OptionalInt.empty(),
                    BuiltInRegistries.RECIPE_BOOK_CATEGORY.byId(0), Optional.empty());
            packet("recipe_" + (items == named ? "tag" : "items"), ClientboundRecipeBookAddPacket.STREAM_CODEC,
                    new ClientboundRecipeBookAddPacket(List.of(new ClientboundRecipeBookAddPacket.Entry(entry, (byte) 3)), false),
                    PacketType.Play.Server.RECIPE_BOOK_ADD);
        }
        Files.write(getDataFolder().toPath().resolve("packet-fixtures.tsv"), fixtures);
        getLogger().info("PACKET_CODECS_OK " + fixtures.size());
    }

    private <T> void packet(String name, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                            T value, PacketTypeCommon type) throws Exception {
        RegistryFriendlyByteBuf input = buffer();
        RegistryFriendlyByteBuf output = buffer();
        try {
            codec.encode(input, value);
            byte[] expected = ByteBufUtil.getBytes(input);
            PacketWrapper<?> wrapper = type.getWrapperClass().getConstructor(PacketSendEvent.class)
                    .newInstance(new TestSendEvent(type, input, testUser));
            require(!input.isReadable(), name + " trailing input");
            wrapper.setBuffer(output);
            wrapper.write();
            require(Arrays.equals(expected, ByteBufUtil.getBytes(output)), name + " differs from Minecraft bytes");
            codec.decode(output);
            require(!output.isReadable(), name + " trailing output");
            fixtures.add(name + "\t" + type.getName() + "\t" + Base64.getEncoder().encodeToString(expected));
        } finally {
            input.release();
            output.release();
        }
    }

    private RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), ((CraftServer) Bukkit.getServer()).getServer().registryAccess());
    }

    private void check(String name, CheckedRunnable runnable) {
        try {
            getDataFolder().mkdirs();
            runnable.run();
        } catch (Throwable error) {
            fail(name, error);
        }
    }

    private void fail(String name, Throwable error) {
        failures.incrementAndGet();
        getLogger().log(Level.SEVERE, "PROBE_FAIL " + name, error);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @Override
    public void onDisable() {
        getLogger().info("PROBE_RESULT failures=" + failures.get() + " livePackets=" + livePackets.get()
                + " packetTypes=" + new TreeSet<>(liveTypes));
    }

    private interface CheckedRunnable { void run() throws Exception; }

    private static final class TestSendEvent extends PacketSendEvent {
        TestSendEvent(PacketTypeCommon type, ByteBuf buffer, User user) {
            super(type.getId(ClientVersion.V_26_3), type, ServerVersion.V_26_3, null, user, null, buffer);
        }
    }
}
