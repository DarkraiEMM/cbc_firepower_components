import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Generates reproducible Ponder structures with Create's checkerboard base
 * plate and the real blocks used by each scene. Scene scripts reveal these
 * saved structure sections and only perform state changes or animation.
 */
public final class GeneratePonderStructures {
	private static final int WIDTH = 9;
	private static final int HEIGHT = 4;
	private static final int DEPTH = 9;

	private static final Map<String, List<PlacedBlock>> NEOFORGE_SCENES = Map.ofEntries(
		Map.entry("mounts/compact", List.of(
			block(4, 1, 4, "cbc_firepower_components:compact_cannon_mount"),
			block(4, 2, 4, "cbc_firepower_components:large_autocannon_breech"),
			block(3, 1, 4, "create:shaft", "axis", "x", "waterlogged", "false"),
			block(4, 1, 3, "minecraft:lever"),
			block(4, 1, 5, "minecraft:lever")
		)),
		Map.entry("mounts/vertical", List.of(
			block(4, 2, 4, "cbc_firepower_components:vertical_compact_cannon_mount"),
			block(4, 3, 4, "cbc_firepower_components:large_autocannon_breech"),
			block(4, 1, 4, "cbc_firepower_components:large_autocannon_breech")
		)),
		Map.entry("large_autocannon/single", List.of(
			block(2, 2, 4, "cbc_firepower_components:large_autocannon_breech"),
			block(3, 2, 4, "cbc_firepower_components:steel_large_autocannon_barrel"),
			block(4, 2, 4, "cbc_firepower_components:steel_thick_large_autocannon_barrel"),
			block(5, 2, 4, "cbc_firepower_components:steel_large_autocannon_muzzle_brake")
		)),
		Map.entry("large_autocannon/twin", List.of(
			block(2, 2, 4, "cbc_firepower_components:twin_large_autocannon_breech"),
			block(3, 2, 4, "cbc_firepower_components:steel_twin_large_autocannon_barrel"),
			block(4, 2, 4, "cbc_firepower_components:steel_twin_large_autocannon_muzzle_brake")
		)),
		Map.entry("ammo/autocannon_feed", List.of(
			block(2, 1, 4, "cbc_firepower_components:large_autocannon_ammo_box"),
			block(4, 1, 4, "cbc_firepower_components:autocannon_ammo_feed"),
			block(6, 1, 4, "cbc_firepower_components:large_autocannon_breech")
		)),
		Map.entry("ammo/magazine_loader", List.of(
			block(4, 1, 4, "cbc_firepower_components:cannon_magazine_loader")
		)),
		Map.entry("ammo/ready_compartment", List.of(
			block(4, 1, 4, "cbc_firepower_components:ready_ammunition_compartment"),
			block(2, 1, 4, "create:mechanical_arm"),
			block(6, 1, 4, "cbc_firepower_components:compact_cannon_mount")
		)),
		Map.entry("ammo/carousel", carouselBlocks()),
		Map.entry("logistics/spent_collector", List.of(
			block(4, 1, 4, "cbc_firepower_components:spent_casing_collector"),
			block(6, 1, 4, "create:mechanical_arm")
		)),
		Map.entry("control/automatic_controller", List.of(
			block(4, 1, 4, "cbc_firepower_components:automatic_cannon_controller"),
			block(4, 1, 3, "minecraft:redstone_wire"),
			block(2, 1, 4, "cbc_firepower_components:compact_autocannon_mount"),
			block(6, 1, 4, "cbc_firepower_components:compact_autocannon_mount"),
			block(4, 1, 6, "cbc_firepower_components:ready_ammunition_compartment")
		)),
		Map.entry("equipment/limiter", List.of(
			block(4, 1, 4, "cbc_firepower_components:compact_cannon_mount")
		)),
		Map.entry("equipment/rangefinding", List.of(
			block(1, 1, 4, "minecraft:lectern"),
			block(7, 1, 4, "minecraft:target")
		))
	);

	private static final Map<String, List<PlacedBlock>> FORGE_SCENES = Map.of(
		"mounts/compact", List.of(
			block(4, 1, 4, "cbc_firepower_components:compact_cannon_mount"),
			block(3, 1, 4, "create:shaft", "axis", "x", "waterlogged", "false"),
			block(4, 1, 3, "minecraft:lever"),
			block(4, 1, 5, "minecraft:lever")
		),
		"ammo/autocannon_feed", List.of(
			block(2, 1, 4, "cbc_firepower_components:large_autocannon_ammo_box"),
			block(5, 1, 4, "cbc_firepower_components:autocannon_ammo_feed")
		),
		"ammo/magazine_loader", List.of(
			block(4, 1, 4, "cbc_firepower_components:cannon_magazine_loader")
		),
		"equipment/limiter", List.of(
			block(4, 1, 4, "cbc_firepower_components:compact_cannon_mount")
		),
		"equipment/shield", List.of(
			block(4, 1, 4, "cbc_firepower_components:sleeve_machine_gun_shield")
		)
	);

	private GeneratePonderStructures() {
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 1)
			throw new IllegalArgumentException("Expected the repository root as the only argument");
		Path root = Path.of(args[0]).toAbsolutePath().normalize();
		writeVersion(root.resolve("versions/neoforge-1.21.1/src/main/resources"), NEOFORGE_SCENES, 3955);
		writeVersion(root.resolve("versions/forge-1.20.1/src/main/resources"), FORGE_SCENES, 3465);
	}

	private static List<PlacedBlock> carouselBlocks() {
		List<PlacedBlock> blocks = new ArrayList<>();
		for (int x = 0; x < 3; ++x) {
			for (int z = 0; z < 3; ++z) {
				if (x == 1 && z == 1)
					blocks.add(block(4, 1, 4, "cbc_firepower_components:carousel_ammunition_rack"));
				else
					blocks.add(block(3 + x, 1, 3 + z,
						"cbc_firepower_components:carousel_ammunition_rack_structure",
						"x_offset", Integer.toString(x), "z_offset", Integer.toString(z)));
			}
		}
		blocks.add(block(4, 1, 7, "cbc_firepower_components:compact_cannon_mount"));
		return List.copyOf(blocks);
	}

	private static PlacedBlock block(int x, int y, int z, String name, String... propertyPairs) {
		if (propertyPairs.length % 2 != 0)
			throw new IllegalArgumentException("Properties must be key/value pairs");
		Map<String, String> properties = new LinkedHashMap<>();
		for (int i = 0; i < propertyPairs.length; i += 2)
			properties.put(propertyPairs[i], propertyPairs[i + 1]);
		return new PlacedBlock(x, y, z, new PaletteEntry(name, properties));
	}

	private static void writeVersion(Path resources, Map<String, List<PlacedBlock>> scenes,
		int dataVersion) throws IOException {
		Path ponderRoot = resources.resolve("assets/cbc_firepower_components/ponder");
		for (Map.Entry<String, List<PlacedBlock>> scene : scenes.entrySet()) {
			Path output = ponderRoot.resolve(scene.getKey() + ".nbt");
			Files.createDirectories(output.getParent());
			writeStructure(output, dataVersion, scene.getValue());
		}
	}

	private static void writeStructure(Path output, int dataVersion, List<PlacedBlock> sceneBlocks)
		throws IOException {
		List<PaletteEntry> palette = new ArrayList<>();
		palette.add(new PaletteEntry("minecraft:white_concrete", Map.of()));
		palette.add(new PaletteEntry("minecraft:snow_block", Map.of()));
		for (PlacedBlock block : sceneBlocks)
			if (!palette.contains(block.state()))
				palette.add(block.state());

		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
			new GZIPOutputStream(Files.newOutputStream(output))))) {
			out.writeByte(10);
			out.writeUTF("");
			writeInt(out, "DataVersion", dataVersion);
			writeIntList(out, "size", WIDTH, HEIGHT, DEPTH);

			out.writeByte(9);
			out.writeUTF("palette");
			out.writeByte(10);
			out.writeInt(palette.size());
			for (PaletteEntry entry : palette)
				writePaletteEntry(out, entry);

			out.writeByte(9);
			out.writeUTF("blocks");
			out.writeByte(10);
			out.writeInt(WIDTH * DEPTH + sceneBlocks.size());
			for (int x = 0; x < WIDTH; ++x) {
				for (int z = 0; z < DEPTH; ++z)
					writeBlock(out, x, 0, z, (x + z) & 1);
			}
			for (PlacedBlock block : sceneBlocks)
				writeBlock(out, block.x(), block.y(), block.z(), palette.indexOf(block.state()));

			out.writeByte(9);
			out.writeUTF("entities");
			out.writeByte(10);
			out.writeInt(0);
			out.writeByte(0);
		}
	}

	private static void writePaletteEntry(DataOutputStream out, PaletteEntry entry) throws IOException {
		writeString(out, "Name", entry.name());
		if (!entry.properties().isEmpty()) {
			out.writeByte(10);
			out.writeUTF("Properties");
			for (Map.Entry<String, String> property : entry.properties().entrySet())
				writeString(out, property.getKey(), property.getValue());
			out.writeByte(0);
		}
		out.writeByte(0);
	}

	private static void writeBlock(DataOutputStream out, int x, int y, int z, int state) throws IOException {
		writeIntList(out, "pos", x, y, z);
		writeInt(out, "state", state);
		out.writeByte(0);
	}

	private static void writeInt(DataOutputStream out, String name, int value) throws IOException {
		out.writeByte(3);
		out.writeUTF(name);
		out.writeInt(value);
	}

	private static void writeString(DataOutputStream out, String name, String value) throws IOException {
		out.writeByte(8);
		out.writeUTF(name);
		out.writeUTF(value);
	}

	private static void writeIntList(DataOutputStream out, String name, int... values) throws IOException {
		out.writeByte(9);
		out.writeUTF(name);
		out.writeByte(3);
		out.writeInt(values.length);
		for (int value : values)
			out.writeInt(value);
	}

	private record PaletteEntry(String name, Map<String, String> properties) {
	}

	private record PlacedBlock(int x, int y, int z, PaletteEntry state) {
	}
}
