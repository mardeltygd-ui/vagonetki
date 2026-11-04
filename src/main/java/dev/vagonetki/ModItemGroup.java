package dev.vagonetki;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

/** Отдельная вкладка в творческом режиме: «Вагонетки». */
public final class ModItemGroup {
	public static final ItemGroup VAGONETKI = Registry.register(
		Registries.ITEM_GROUP,
		Vagonetki.id("main"),
		FabricItemGroup.builder()
			.icon(() -> new ItemStack(ModItems.PLANE_CART))
			.displayName(Text.translatable("itemGroup.vagonetki.main"))
			.entries((context, entries) -> {
				entries.add(ModItems.PLANE_CART);
				entries.add(ModItems.BOAT_CART);
				entries.add(ModItems.SLIME_CART);
				entries.add(ModItems.UNIVERSAL_CART);
				entries.add(ModItems.PLANE_RAIL);
				entries.add(ModItems.BOAT_RAIL);
				entries.add(ModItems.UNIVERSAL_RAIL);
			})
			.build()
	);

	private ModItemGroup() {
	}

	public static void register() {
		// Вызов нужен, чтобы класс загрузился и вкладка зарегистрировалась.
	}
}
