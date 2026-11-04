package dev.vagonetki;

import dev.vagonetki.entity.BoatCartEntity;
import dev.vagonetki.entity.CartKind;
import dev.vagonetki.entity.ModCartEntity;
import dev.vagonetki.entity.PlaneCartEntity;
import dev.vagonetki.entity.SlimeCartEntity;
import dev.vagonetki.entity.UniversalCartEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;

public final class ModEntities {
	public static final EntityType<PlaneCartEntity> PLANE_CART = register("plane_cart", PlaneCartEntity::new);
	public static final EntityType<BoatCartEntity> BOAT_CART = register("boat_cart", BoatCartEntity::new);
	public static final EntityType<SlimeCartEntity> SLIME_CART = register("slime_cart", SlimeCartEntity::new);
	public static final EntityType<UniversalCartEntity> UNIVERSAL_CART = register("universal_cart", UniversalCartEntity::new);

	private ModEntities() {
	}

	private static <T extends ModCartEntity> EntityType<T> register(String name, EntityType.EntityFactory<T> factory) {
		return Registry.register(
			Registries.ENTITY_TYPE,
			Vagonetki.id(name),
			FabricEntityTypeBuilder.<T>create(SpawnGroup.MISC, factory)
				.dimensions(EntityDimensions.fixed(0.98f, 0.7f))
				.trackRangeBlocks(8)
				.trackedUpdateRate(1)
				.forceTrackedVelocityUpdates(true)
				.build()
		);
	}

	public static ModCartEntity create(CartKind kind, World world, double x, double y, double z) {
		return switch (kind) {
			case PLANE -> new PlaneCartEntity(world, x, y, z);
			case BOAT -> new BoatCartEntity(world, x, y, z);
			case SLIME -> new SlimeCartEntity(world, x, y, z);
			case UNIVERSAL -> new UniversalCartEntity(world, x, y, z);
		};
	}

	public static void register() {
		// Вызов нужен, чтобы класс загрузился и типы сущностей зарегистрировались.
	}
}
