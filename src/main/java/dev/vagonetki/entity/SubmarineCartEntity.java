package dev.vagonetki.entity;

import dev.vagonetki.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class SubmarineCartEntity extends ModCartEntity {
	public SubmarineCartEntity(EntityType<? extends SubmarineCartEntity> type, World world) {
		super(type, world);
	}

	public SubmarineCartEntity(World world, double x, double y, double z) {
		super(ModEntities.SUBMARINE_CART, world, x, y, z);
	}

	@Override
	public CartKind kind() {
		return CartKind.SUBMARINE;
	}
}
