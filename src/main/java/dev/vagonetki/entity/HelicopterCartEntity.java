package dev.vagonetki.entity;

import dev.vagonetki.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class HelicopterCartEntity extends ModCartEntity {
	public HelicopterCartEntity(EntityType<? extends HelicopterCartEntity> type, World world) {
		super(type, world);
	}

	public HelicopterCartEntity(World world, double x, double y, double z) {
		super(ModEntities.HELICOPTER_CART, world, x, y, z);
	}

	@Override
	public CartKind kind() {
		return CartKind.HELICOPTER;
	}
}
