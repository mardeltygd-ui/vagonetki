package dev.vagonetki.entity;

import dev.vagonetki.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class LavaCartEntity extends ModCartEntity {
	public LavaCartEntity(EntityType<? extends LavaCartEntity> type, World world) {
		super(type, world);
	}

	public LavaCartEntity(World world, double x, double y, double z) {
		super(ModEntities.LAVA_CART, world, x, y, z);
	}

	@Override
	public CartKind kind() {
		return CartKind.LAVA;
	}
}
