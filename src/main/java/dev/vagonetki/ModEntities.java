package dev.vagonetki;

import dev.vagonetki.entity.BoatCartEntity;
import dev.vagonetki.entity.CartKind;
import dev.vagonetki.entity.EnderCartEntity;
import dev.vagonetki.entity.HelicopterCartEntity;
import dev.vagonetki.entity.IceCartEntity;
import dev.vagonetki.entity.LavaCartEntity;
import dev.vagonetki.entity.ModCartEntity;
import dev.vagonetki.entity.PlaneCartEntity;
import dev.vagonetki.entity.RocketCartEntity;
import dev.vagonetki.entity.SlimeCartEntity;
import dev.vagonetki.entity.SubmarineCartEntity;
import dev.vagonetki.entity.UniversalCartEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;

public final class ModEntities {
	public static final EntityType<PlaneCartEntity> PLANE_CART = register(CartKind.PLANE, PlaneCartEntity::new);
	public static final EntityType<BoatCartEntity> BOAT_CART = register(CartKind.BOAT, BoatCartEntity::new);
	public static final EntityType<SlimeCartEntity> SLIME_CART = register(CartKind.SLIME, SlimeCartEntity::new);
	public static final EntityType<UniversalCartEntity> UNIVERSAL_CART = register(CartKind.UNIVERSAL, UniversalCartEntity::new);
	public static final EntityType<HelicopterCartEntity> HELICOPTER_CART = register(CartKind.HELICOPTER, HelicopterCartEntity::new);
	public static final EntityType<SubmarineCartEntity> SUBMARINE_CART = register(CartKind.SUBMARINE, SubmarineCartEntity::new);
	public static final EntityType<IceCartEntity> ICE_CART = register(CartKind.ICE, IceCartEntity::new);
	public static final EntityType<LavaCartEntity> LAVA_CART = register(CartKind.LAVA, LavaCartEntity::new);
	public static final EntityType<EnderCartEntity> ENDER_CART = register(CartKind.ENDER, EnderCartEntity::new);
	public static final EntityType<RocketCartEntity> ROCKET_CART = register(CartKind.ROCKET, RocketCartEntity::new);

	private ModEntities() {
	}

	private static <T extends ModCartEntity> EntityType<T> register(CartKind kind, EntityType.EntityFactory<T> factory) {
		return Registry.register(
			Registries.ENTITY_TYPE,
			Vagonetki.id(kind.cartId()),
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
			case HELICOPTER -> new HelicopterCartEntity(world, x, y, z);
			case SUBMARINE -> new SubmarineCartEntity(world, x, y, z);
			case ICE -> new IceCartEntity(world, x, y, z);
			case LAVA -> new LavaCartEntity(world, x, y, z);
			case ENDER -> new EnderCartEntity(world, x, y, z);
			case ROCKET -> new RocketCartEntity(world, x, y, z);
		};
	}

	public static void register() {
		// Вызов нужен, чтобы класс загрузился и типы сущностей зарегистрировались.
	}
}
