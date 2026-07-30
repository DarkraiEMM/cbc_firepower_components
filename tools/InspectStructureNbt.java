import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Minimal read-only structure-NBT inspector used to compare generated Ponder
 * canvases with Create's own scene structures.
 */
public final class InspectStructureNbt {
	private InspectStructureNbt() {
	}

	public static void main(String[] args) throws IOException {
		for (String arg : args)
			inspect(Path.of(arg));
	}

	@SuppressWarnings("unchecked")
	private static void inspect(Path path) throws IOException {
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(
			new GZIPInputStream(Files.newInputStream(path))))) {
			int rootType = in.readUnsignedByte();
			if (rootType != 10)
				throw new IOException("Root tag is not a compound: " + rootType);
			in.readUTF();
			Map<String, Object> root = (Map<String, Object>) readPayload(in, rootType);
			List<Object> palette = (List<Object>) root.get("palette");
			List<Object> blocks = (List<Object>) root.get("blocks");
			System.out.println("== " + path);
			System.out.println("size=" + root.get("size"));
			for (int i = 0; i < palette.size(); ++i)
				System.out.println("palette[" + i + "]=" + palette.get(i));
			Map<Integer, Integer> counts = new LinkedHashMap<>();
			Map<Integer, Integer> layers = new LinkedHashMap<>();
			for (Object raw : blocks) {
				Map<String, Object> block = (Map<String, Object>) raw;
				int state = (Integer) block.get("state");
				List<Object> pos = (List<Object>) block.get("pos");
				counts.merge(state, 1, Integer::sum);
				layers.merge((Integer) pos.get(1), 1, Integer::sum);
			}
			System.out.println("stateCounts=" + counts);
			System.out.println("layerCounts=" + layers);
		}
	}

	private static Object readPayload(DataInputStream in, int type) throws IOException {
		return switch (type) {
			case 1 -> in.readByte();
			case 2 -> in.readShort();
			case 3 -> in.readInt();
			case 4 -> in.readLong();
			case 5 -> in.readFloat();
			case 6 -> in.readDouble();
			case 7 -> {
				byte[] value = new byte[in.readInt()];
				in.readFully(value);
				yield value;
			}
			case 8 -> in.readUTF();
			case 9 -> {
				int elementType = in.readUnsignedByte();
				int size = in.readInt();
				List<Object> values = new ArrayList<>(size);
				for (int i = 0; i < size; ++i)
					values.add(readPayload(in, elementType));
				yield values;
			}
			case 10 -> {
				Map<String, Object> values = new LinkedHashMap<>();
				while (true) {
					int childType = in.readUnsignedByte();
					if (childType == 0)
						break;
					values.put(in.readUTF(), readPayload(in, childType));
				}
				yield values;
			}
			case 11 -> {
				int[] value = new int[in.readInt()];
				for (int i = 0; i < value.length; ++i)
					value[i] = in.readInt();
				yield value;
			}
			case 12 -> {
				long[] value = new long[in.readInt()];
				for (int i = 0; i < value.length; ++i)
					value[i] = in.readLong();
				yield value;
			}
			default -> throw new IOException("Unsupported tag type: " + type);
		};
	}
}
