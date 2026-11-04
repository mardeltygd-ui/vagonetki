package dev.vagonetki;

import dev.vagonetki.entity.CartKind;
import dev.vagonetki.item.CartItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ModItems {
	private static final Map<CartKind, Item> CARTS = new EnumMap<>(CartKind.class);
	private static final List<Item> RAILS = new ArrayList<>();

	public static final Item PLANE_CART = cart(CartKind.PLANE);
	public static final Item BOAT_CART = cart(CartKind.BOAT);
	public static final Item SLIME_CART = cart(CartKind.SLIME);
	public static final Item UNIVERSAL_CART = cart(CartKind.UNIVERSAL);
	public static final Item HELICOPTER_CART = cart(CartKind.HELICOPTER);
	public static final Item SUBMARINE_CART = cart(CartKind.SUBMARINE);
	public static final Item ICE_CART = cart(CartKind.ICE);
	public static final Item LAVA_CART = cart(CartKind.LAVA);
	public static final Item ENDER_CART = cart(CartKind.ENDER);
	public static final Item ROCKET_CART = cart(CartKind.ROCKET);

	static {
		for (CartKind kind : CartKind.values()) {
			if (kind != CartKind.SLIME) {
				RAILS.add(Registry.register(
					Registries.ITEM,
					Vagonetki.id(kind.railId()),
					new BlockItem(ModBlocks.railFor(kind), new FabricItemSettings())
				));
			}
		}
	}

	private ModItems() {
	}

	private static Item cart(CartKind kind) {
		Item item = Registry.register(Registries.ITEM, Vagonetki.id(kind.cartId()), new CartItem(kind, new FabricItemSettings().maxCount(1)));
		CARTS.put(kind, item);
		return item;
	}

	public static Item forKind(CartKind kind) {
		return CARTS.get(kind);
	}

	public static List<Item> rails() {
		return RAILS;
	}

	public static void register() {
		// Вызов нужен, чтобы класс загрузился и предметы зарегистрировались.
	}
}
