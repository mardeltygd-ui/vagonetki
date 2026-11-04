package dev.vagonetki.entity;

import dev.vagonetki.ModBlocks;
import dev.vagonetki.Vagonetki;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

/** Все виды вагонеток и то, что каждая из них умеет. */
public enum CartKind {
	PLANE("plane", Cap.FLIES | Cap.FLIPS | Cap.EXTRAS),
	BOAT("boat", Cap.WATER | Cap.EXTRAS),
	SLIME("slime", Cap.JUMPS),
	UNIVERSAL("universal", Cap.FLIES | Cap.FLIPS | Cap.WATER | Cap.JUMPS | Cap.EXTRAS),
	HELICOPTER("helicopter", Cap.HOVERS | Cap.EXTRAS),
	SUBMARINE("submarine", Cap.DIVES | Cap.EXTRAS),
	ICE("ice", Cap.GLIDES | Cap.EXTRAS),
	LAVA("lava", Cap.LAVA | Cap.EXTRAS),
	ENDER("ender", Cap.BLINKS | Cap.EXTRAS),
	ROCKET("rocket", Cap.FLIES | Cap.EXTRAS);

	private static final class Cap {
		static final int FLIES = 1;
		static final int HOVERS = 2;
		static final int WATER = 4;
		static final int LAVA = 8;
		static final int DIVES = 16;
		static final int GLIDES = 32;
		static final int BLINKS = 64;
		static final int JUMPS = 128;
		static final int FLIPS = 256;
		static final int EXTRAS = 512;
	}

	/** Имя без суффикса: plane, boat, ... Предмет называется plane_cart, рельс plane_rail. */
	public final String name;
	public final boolean flies;
	public final boolean hovers;
	public final boolean floatsWater;
	public final boolean floatsLava;
	public final boolean dives;
	public final boolean glides;
	public final boolean blinks;
	public final boolean jumps;
	public final boolean canFlip;
	public final boolean extras;

	CartKind(String name, int caps) {
		this.name = name;
		this.flies = (caps & Cap.FLIES) != 0;
		this.hovers = (caps & Cap.HOVERS) != 0;
		this.floatsWater = (caps & Cap.WATER) != 0;
		this.floatsLava = (caps & Cap.LAVA) != 0;
		this.dives = (caps & Cap.DIVES) != 0;
		this.glides = (caps & Cap.GLIDES) != 0;
		this.blinks = (caps & Cap.BLINKS) != 0;
		this.jumps = (caps & Cap.JUMPS) != 0;
		this.canFlip = (caps & Cap.FLIPS) != 0;
		this.extras = (caps & Cap.EXTRAS) != 0;
	}

	public String cartId() {
		return name + "_cart";
	}

	public String railId() {
		return name + "_rail";
	}

	/** Слизь-вагонетка работает на любых рельсах и не требует включённых рельсов. */
	public boolean needsPoweredRail() {
		return this != SLIME;
	}

	/** Управляется клавишами. Слизь только прыгает. */
	public boolean drives() {
		return this != SLIME;
	}

	public boolean hasExtras() {
		return extras;
	}

	public Identifier bodyTexture() {
		return Vagonetki.id("textures/entity/" + cartId() + ".png");
	}

	/** Можно ли поставить эту вагонетку на данный блок. */
	public boolean acceptsRail(BlockState state) {
		if (!state.isIn(BlockTags.RAILS)) {
			return false;
		}
		return this == SLIME || state.isOf(ModBlocks.railFor(this));
	}
}
