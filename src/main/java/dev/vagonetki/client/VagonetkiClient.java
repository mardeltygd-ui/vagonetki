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

	/** Стрелка вверх: набор высоты (самолёт, вертолёт, подлодка, ракета). Можно переназначить. */
	public static KeyBinding upKey;
	/** R: бочка (самолёт). */
	public static KeyBinding rollKey;
	/** G: петля (самолёт). */
	public static KeyBinding loopKey;

	private static KeyBinding key(String name, int code) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(name, InputUtil.Type.KEYSYM, code, "key.categories.vagonetki"));
	}

	@Override
	public void onInitializeClient() {
		upKey = key("key.vagonetki.up", GLFW.GLFW_KEY_UP);
		rollKey = key("key.vagonetki.roll", GLFW.GLFW_KEY_R);
		loopKey = key("key.vagonetki.loop", GLFW.GLFW_KEY_G);

		EntityModelLayerRegistry.registerModelLayer(EXTRAS_LAYER, CartExtrasModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.PLANE_CART, ctx -> new ModCartRenderer<>(ctx));
		EntityRendererRegistry.register(ModEntities.BOAT_CART, ctx -> new ModCartRenderer<>(ctx));
		EntityRendererRegistry.register(ModEntities.SLIME_CART, ctx -> new ModCartRenderer<>(ctx));
		EntityRendererRegistry.register(ModEntities.UNIVERSAL_CART, ctx -> new ModCartRenderer<>(ctx));
		EntityRendererRegistry.register(ModEntities.HELICOPTER_CART, ctx -> new ModCartRenderer<>(ctx));
		EntityRendererRegistry.register(ModEntities.SUBMARINE_CART, ctx -> new ModCartRenderer<>(ctx));
		EntityRendererRegistry.register(ModEntities.ICE_CART, ctx -> new ModCartRenderer<>(ctx));
		EntityRendererRegistry.register(ModEntities.LAVA_CART, ctx -> new ModCartRenderer<>(ctx));
		EntityRendererRegistry.register(ModEntities.ENDER_CART, ctx -> new ModCartRenderer<>(ctx));
		EntityRendererRegistry.register(ModEntities.ROCKET_CART, ctx -> new ModCartRenderer<>(ctx));

		// Каждый тик, пока игрок сидит в нашей вагонетке, шлём на сервер нажатые клавиши.
		// W/A/S/D (на русской раскладке Ц/Ф/Ы/В, это те же физические клавиши), пробел, стрелка вверх, R и G.
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
			if (rollKey.isPressed()) bits |= ModCartEntity.F_ROLL;
			if (loopKey.isPressed()) bits |= ModCartEntity.F_LOOP;
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeByte(bits);
			ClientPlayNetworking.send(Net.CONTROLS, buf);
		});
	}
}
