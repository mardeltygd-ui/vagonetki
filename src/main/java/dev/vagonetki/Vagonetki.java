package dev.vagonetki;

import dev.vagonetki.entity.ModCartEntity;
import dev.vagonetki.net.Net;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public class Vagonetki implements ModInitializer {
	public static final String MOD_ID = "vagonetki";

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModBlocks.register();
		ModEntities.register();
		ModItems.register();
		ModItemGroup.register();
		Net.registerServer();

		// ЛКМ по вагонетке с блоком в руке: блок ставится внутрь вагонетки (для вида).
		// С пустой рукой, с любым другим предметом или с Shift вагонетку можно сломать как обычно.
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (entity instanceof ModCartEntity cart && !player.isSpectator() && !player.isSneaking()) {
				ItemStack stack = player.getStackInHand(hand);
				if (stack.getItem() instanceof BlockItem blockItem) {
					if (!world.isClient) {
						cart.setCustomBlock(blockItem.getBlock().getDefaultState());
					}
					return ActionResult.SUCCESS;
				}
			}
			return ActionResult.PASS;
		});

		// Встроенный ресурспак «Classic» (включается в меню «Ресурспаки»).
		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container ->
			ResourceManagerHelper.registerBuiltinResourcePack(
				id("classic"),
				container,
				Text.literal("Vagonetki Classic"),
				ResourcePackActivationType.NORMAL
			)
		);
	}
}
