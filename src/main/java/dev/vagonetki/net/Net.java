package dev.vagonetki.net;

import dev.vagonetki.Vagonetki;
import dev.vagonetki.entity.ModCartEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public final class Net {
	/** Клиент -> сервер: один байт с нажатыми клавишами (см. ModCartEntity.F_*). */
	public static final Identifier CONTROLS = Vagonetki.id("controls");

	private Net() {
	}

	public static void registerServer() {
		ServerPlayNetworking.registerGlobalReceiver(CONTROLS, (server, player, handler, buf, responseSender) -> {
			int bits = buf.readUnsignedByte();
			server.execute(() -> {
				if (player.getVehicle() instanceof ModCartEntity cart) {
					cart.setControls(bits);
				}
			});
		});
	}
}
