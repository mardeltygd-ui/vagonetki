package dev.vagonetki.entity;

import dev.vagonetki.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class UniversalCartEntity extends ModCartEntity {
	public UniversalCartEntity(EntityType<? extends UniversalCartEntity> type, World world) {
		super(type, world);
	}

	public UniversalCartEntity(World world, double x, double y, double z) {
		super(ModEntities.UNIVERSAL_CART, world, x, y, z);
	}

	@Override
	public CartKind kind() {
		return CartKind.UNIVERSAL;
	}
}
