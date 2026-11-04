package dev.vagonetki;

import dev.vagonetki.entity.CartKind;
import dev.vagonetki.item.CartItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModItems {
	public static final Item PLANE_RAIL = register("plane_rail", new BlockItem(ModBlocks.PLANE_RAIL, new FabricItemSettings()));
	public static final Item BOAT_RAIL = register("boat_rail", new BlockItem(ModBlocks.BOAT_RAIL, new FabricItemSettings()));
	public static final Item UNIVERSAL_RAIL = register("universal_rail", new BlockItem(ModBlocks.UNIVERSAL_RAIL, new FabricItemSettings()));

	public static final Item PLANE_CART = register("plane_cart", new CartItem(CartKind.PLANE, new FabricItemSettings().maxCount(1)));
	public static final Item BOAT_CART = register("boat_cart", new CartItem(CartKind.BOAT, new FabricItemSettings().maxCount(1)));
	public static final Item SLIME_CART = register("slime_cart", new CartItem(CartKind.SLIME, new FabricItemSettings().maxCount(1)));
	public static final Item UNIVERSAL_CART = register("universal_cart", new CartItem(CartKind.UNIVERSAL, new FabricItemSettings().maxCount(1)));

	private ModItems() {
	}

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, Vagonetki.id(name), item);
	}

	public static Item forKind(CartKind kind) {
		return switch (kind) {
			case PLANE -> PLANE_CART;
			case BOAT -> BOAT_CART;
			case SLIME -> SLIME_CART;
			case UNIVERSAL -> UNIVERSAL_CART;
		};
	}

	public static void register() {
		// Вызов нужен, чтобы класс загрузился и предметы зарегистрировались.
	}
}
