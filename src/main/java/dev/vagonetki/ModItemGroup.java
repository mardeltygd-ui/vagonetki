package dev.vagonetki;

import dev.vagonetki.entity.CartKind;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
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
				for (CartKind kind : CartKind.values()) {
					entries.add(ModItems.forKind(kind));
				}
				for (Item rail : ModItems.rails()) {
					entries.add(rail);
				}
			})
			.build()
	);

	private ModItemGroup() {
	}

	public static void register() {
		// Вызов нужен, чтобы класс загрузился и вкладка зарегистрировалась.
	}
}
