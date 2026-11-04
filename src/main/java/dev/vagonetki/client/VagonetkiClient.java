package dev.vagonetki.client;

import dev.vagonetki.ModEntities;
import dev.vagonetki.Vagonetki;
import dev.vagonetki.entity.ModCartEntity;
import dev.vagonetki.net.Net;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;

public class VagonetkiClient implements ClientModInitializer {
	public static final EntityModelLayer EXTRAS_LAYER = new EntityModelLayer(Vagonetki.id("cart_extras"), "main");

	/** Стрелка вверх: набор высоты у самолёта. Можно переназначить в настройках управления. */
	public static KeyBinding upKey;

	@Override
	public void onInitializeClient() {
		upKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.vagonetki.up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UP, "key.categories.vagonetki"));

		EntityModelLayerRegistry.registerModelLayer(EXTRAS_LAYER, CartExtrasModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.PLANE_CART, ctx -> new ModCartRenderer<>(ctx));
		EntityRendererRegistry.register(ModEntities.BOAT_CART, ctx -> new ModCartRenderer<>(ctx));
		EntityRendererRegistry.register(ModEntities.SLIME_CART, ctx -> new ModCartRenderer<>(ctx));
		EntityRendererRegistry.register(ModEntities.UNIVERSAL_CART, ctx -> new ModCartRenderer<>(ctx));

		// Каждый тик, пока игрок сидит в нашей вагонетке, шлём на сервер нажатые клавиши.
		// W/A/S/D (на русской раскладке Ц/Ф/Ы/В, это те же физические клавиши), пробел и стрелка вверх.
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || !(client.player.getVehicle() instanceof ModCartEntity)) {
				return;
			}
			GameOptions o = client.options;
			int bits = 0;
			if (o.forwardKey.isPressed()) bits |= ModCartEntity.F_FORWARD;
			if (o.backKey.isPressed()) bits |= ModCartEntity.F_BACK;
			if (o.leftKey.isPressed()) bits |= ModCartEntity.F_LEFT;
			if (o.rightKey.isPressed()) bits |= ModCartEntity.F_RIGHT;
			if (upKey.isPressed()) bits |= ModCartEntity.F_UP;
			if (o.jumpKey.isPressed()) bits |= ModCartEntity.F_JUMP;
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeByte(bits);
			ClientPlayNetworking.send(Net.CONTROLS, buf);
		});
	}
}
