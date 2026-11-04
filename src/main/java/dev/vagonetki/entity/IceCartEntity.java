package dev.vagonetki.entity;

import dev.vagonetki.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class IceCartEntity extends ModCartEntity {
	public IceCartEntity(EntityType<? extends IceCartEntity> type, World world) {
		super(type, world);
	}

	public IceCartEntity(World world, double x, double y, double z) {
		super(ModEntities.ICE_CART, world, x, y, z);
	}

	@Override
	public CartKind kind() {
		return CartKind.ICE;
	}
}
