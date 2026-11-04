package dev.vagonetki.entity;

import dev.vagonetki.ModBlocks;
import dev.vagonetki.Vagonetki;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

/** Четыре вида вагонеток и то, что каждая из них умеет. */
public enum CartKind {
	PLANE("plane_cart", true, false, false),
	BOAT("boat_cart", false, true, false),
	SLIME("slime_cart", false, false, true),
	UNIVERSAL("universal_cart", true, true, true);

	public final String id;
	/** Умеет летать. */
	public final boolean flies;
	/** Умеет плавать. */
	public final boolean floats;
	/** Умеет прыгать по пробелу. */
	public final boolean jumps;

	CartKind(String id, boolean flies, boolean floats, boolean jumps) {
		this.id = id;
		this.flies = flies;
		this.floats = floats;
		this.jumps = jumps;
	}

	/** Слизь-вагонетка работает на любых рельсах и не требует включённых рельсов. */
	public boolean needsPoweredRail() {
		return this != SLIME;
	}

	/** Управляется клавишами (самолёт, лодка, универсальная). Слизь только прыгает. */
	public boolean drives() {
		return this != SLIME;
	}

	public boolean hasExtras() {
		return this != SLIME;
	}

	public Identifier bodyTexture() {
		return Vagonetki.id("textures/entity/" + id + ".png");
	}

	/** Можно ли поставить эту вагонетку на данный блок. */
	public boolean acceptsRail(BlockState state) {
		if (!state.isIn(BlockTags.RAILS)) {
			return false;
		}
		return switch (this) {
			case SLIME -> true;
			case PLANE -> state.isOf(ModBlocks.PLANE_RAIL);
			case BOAT -> state.isOf(ModBlocks.BOAT_RAIL);
			case UNIVERSAL -> state.isOf(ModBlocks.UNIVERSAL_RAIL);
		};
	}
}
