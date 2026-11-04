package dev.vagonetki.entity;

import dev.vagonetki.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class EnderCartEntity extends ModCartEntity {
	public EnderCartEntity(EntityType<? extends EnderCartEntity> type, World world) {
		super(type, world);
	}

	public EnderCartEntity(World world, double x, double y, double z) {
		super(ModEntities.ENDER_CART, world, x, y, z);
	}

	@Override
	public CartKind kind() {
		return CartKind.ENDER;
	}
}
