package dev.vagonetki.entity;

import dev.vagonetki.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class BoatCartEntity extends ModCartEntity {
	public BoatCartEntity(EntityType<? extends BoatCartEntity> type, World world) {
		super(type, world);
	}

	public BoatCartEntity(World world, double x, double y, double z) {
		super(ModEntities.BOAT_CART, world, x, y, z);
	}

	@Override
	public CartKind kind() {
		return CartKind.BOAT;
	}
}
