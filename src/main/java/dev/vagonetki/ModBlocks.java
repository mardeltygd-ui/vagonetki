package dev.vagonetki;

import dev.vagonetki.block.CartRailBlock;
import dev.vagonetki.entity.CartKind;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.Properties;

/** Цветные рельсы. Каждый включается одним блоком редстоуна вплотную к рельсу. */
public final class ModBlocks {
	public static final Block PLANE_RAIL = rail(CartKind.PLANE);
	public static final Block BOAT_RAIL = rail(CartKind.BOAT);
	public static final Block UNIVERSAL_RAIL = rail(CartKind.UNIVERSAL);
	public static final Block HELICOPTER_RAIL = rail(CartKind.HELICOPTER);
	public static final Block SUBMARINE_RAIL = rail(CartKind.SUBMARINE);
	public static final Block ICE_RAIL = rail(CartKind.ICE);
	public static final Block LAVA_RAIL = rail(CartKind.LAVA);
	public static final Block ENDER_RAIL = rail(CartKind.ENDER);
	public static final Block ROCKET_RAIL = rail(CartKind.ROCKET);

	private ModBlocks() {
	}

	private static Block rail(CartKind kind) {
		AbstractBlock.Settings settings = AbstractBlock.Settings.copy(Blocks.POWERED_RAIL)
			.luminance(state -> state.get(Properties.POWERED) ? 7 : 0);
		return Registry.register(Registries.BLOCK, Vagonetki.id(kind.railId()), new CartRailBlock(settings, kind, 1));
	}

	public static Block railFor(CartKind kind) {
		return switch (kind) {
			case PLANE -> PLANE_RAIL;
			case BOAT -> BOAT_RAIL;
			case UNIVERSAL -> UNIVERSAL_RAIL;
			case HELICOPTER -> HELICOPTER_RAIL;
			case SUBMARINE -> SUBMARINE_RAIL;
			case ICE -> ICE_RAIL;
			case LAVA -> LAVA_RAIL;
			case ENDER -> ENDER_RAIL;
			case ROCKET -> ROCKET_RAIL;
			case SLIME -> throw new IllegalArgumentException("У слизь-вагонетки нет своего рельса");
		};
	}

	public static void register() {
		// Вызов нужен, чтобы класс загрузился и блоки зарегистрировались.
	}
}
