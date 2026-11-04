package dev.vagonetki.entity;

import dev.vagonetki.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class RocketCartEntity extends ModCartEntity {
	public RocketCartEntity(EntityType<? extends RocketCartEntity> type, World world) {
		super(type, world);
	}

	public RocketCartEntity(World world, double x, double y, double z) {
		super(ModEntities.ROCKET_CART, world, x, y, z);
	}

	@Override
	public CartKind kind() {
		return CartKind.ROCKET;
	}
}
