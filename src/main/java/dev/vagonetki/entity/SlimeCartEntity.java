package dev.vagonetki.entity;

import dev.vagonetki.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class SlimeCartEntity extends ModCartEntity {
	public SlimeCartEntity(EntityType<? extends SlimeCartEntity> type, World world) {
		super(type, world);
	}

	public SlimeCartEntity(World world, double x, double y, double z) {
		super(ModEntities.SLIME_CART, world, x, y, z);
	}

	@Override
	public CartKind kind() {
		return CartKind.SLIME;
	}
}
