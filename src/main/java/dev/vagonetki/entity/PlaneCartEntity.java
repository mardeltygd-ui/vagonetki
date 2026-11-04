package dev.vagonetki.entity;

import dev.vagonetki.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class PlaneCartEntity extends ModCartEntity {
	public PlaneCartEntity(EntityType<? extends PlaneCartEntity> type, World world) {
		super(type, world);
	}

	public PlaneCartEntity(World world, double x, double y, double z) {
		super(ModEntities.PLANE_CART, world, x, y, z);
	}

	@Override
	public CartKind kind() {
		return CartKind.PLANE;
	}
}
