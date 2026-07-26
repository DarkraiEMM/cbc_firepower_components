import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Generates the blank 9x4x9 structure canvases used by the scripted Ponder
 * scenes. Keeping this generator in the repository makes the binary NBT
 * resources reproducible and reviewable.
 */
public final class GeneratePonderStructures {
	private static final int WIDTH = 9;
	private static final int HEIGHT = 4;
	private static final int DEPTH = 9;

	private static final List<String> NEOFORGE_SCENES = List.of(
		"mounts/compact",
		"mounts/vertical",
		"large_autocannon/single",
		"large_autocannon/twin",
		"ammo/autocannon_feed",
		"ammo/magazine_loader",
		"ammo/ready_compartment",
		"ammo/carousel",
		"logistics/spent_collector",
		"control/automatic_controller",
		"equipment/limiter",
		"equipment/rangefinding"
	);

	private static final List<String> FORGE_SCENES = List.of(
		"mounts/compact",
		"ammo/autocannon_feed",
		"ammo/magazine_loader",
		"equipment/limiter",
		"equipment/shield"
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

	private static void writeVersion(Path resources, List<String> scenes, int dataVersion) throws IOException {
		Path ponderRoot = resources.resolve("assets/cbc_firepower_components/ponder");
		for (String scene : scenes) {
			Path output = ponderRoot.resolve(scene + ".nbt");
			Files.createDirectories(output.getParent());
			writeCanvas(output, dataVersion);
		}
	}

	private static void writeCanvas(Path output, int dataVersion) throws IOException {
		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
			new GZIPOutputStream(Files.newOutputStream(output))))) {
			out.writeByte(10);
			out.writeUTF("");

			writeInt(out, "DataVersion", dataVersion);
			writeIntList(out, "size", WIDTH, HEIGHT, DEPTH);

			out.writeByte(9);
			out.writeUTF("palette");
			out.writeByte(10);
			out.writeInt(1);
			writeString(out, "Name", "minecraft:gray_concrete");
			out.writeByte(0);

			out.writeByte(9);
			out.writeUTF("blocks");
			out.writeByte(10);
			out.writeInt(WIDTH * DEPTH);
			for (int x = 0; x < WIDTH; ++x) {
				for (int z = 0; z < DEPTH; ++z) {
					writeIntList(out, "pos", x, 0, z);
					writeInt(out, "state", 0);
					out.writeByte(0);
				}
			}

			out.writeByte(9);
			out.writeUTF("entities");
			out.writeByte(10);
			out.writeInt(0);
			out.writeByte(0);
		}
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
}
