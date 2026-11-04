package dev.vagonetki;

import dev.vagonetki.block.CartRailBlock;
import dev.vagonetki.entity.CartKind;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.Properties;

public final class ModBlocks {
	/** Чёрные рельсы для самолёта. Включаются одним блоком редстоуна рядом. */
	public static final Block PLANE_RAIL = register("plane_rail", new CartRailBlock(settings(), CartKind.PLANE, 1));
	/** Зелёные рельсы для лодки. Один блок редстоуна. */
	public static final Block BOAT_RAIL = register("boat_rail", new CartRailBlock(settings(), CartKind.BOAT, 1));
	/** Синие рельсы для универсальной вагонетки. Два блока редстоуна. */
	public static final Block UNIVERSAL_RAIL = register("universal_rail", new CartRailBlock(settings(), CartKind.UNIVERSAL, 2));

	private ModBlocks() {
	}

	private static AbstractBlock.Settings settings() {
		return AbstractBlock.Settings.copy(Blocks.POWERED_RAIL)
			.luminance(state -> state.get(Properties.POWERED) ? 7 : 0);
	}

	private static Block register(String name, Block block) {
		return Registry.register(Registries.BLOCK, Vagonetki.id(name), block);
	}

	public static void register() {
		// Вызов нужен, чтобы класс загрузился и блоки зарегистрировались.
	}
}
