package dev.vagonetki.entity;

import dev.vagonetki.block.CartRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

/**
 * Общая часть всех вагонеток мода.
 *
 * Пока вагонетка не «заведена» (или нет игрока внутри), она ездит по рельсам как обычная.
 * Заводится она, когда игрок сидит внутри, а она стоит на включённых рельсах своего цвета.
 * После этого она свободно едет, летит или плывёт по клавишам и больше от рельсов не зависит,
 * пока игрок не выйдет.
 */
public abstract class ModCartEntity extends AbstractMinecartEntity {
	public static final int F_FORWARD = 1;
	public static final int F_BACK = 2;
	public static final int F_LEFT = 4;
	public static final int F_RIGHT = 8;
	public static final int F_UP = 16;
	public static final int F_JUMP = 32;

	/** Куда смотрит нос вагонетки, в градусах: вперёд = (cos, sin) по осям X и Z. */
	private static final TrackedData<Float> HEADING = DataTracker.registerData(ModCartEntity.class, TrackedDataHandlerRegistry.FLOAT);

	private int controls;
	private int controlAge;
	private boolean armed;
	private boolean flightMode;
	private boolean airborne;

	private boolean headingInit;
	private float renderHeading;
	private float prevRenderHeading;

	protected ModCartEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	protected ModCartEntity(EntityType<?> type, World world, double x, double y, double z) {
		super(type, world, x, y, z);
	}

	public abstract CartKind kind();

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(HEADING, 0.0f);
	}

	@Override
	public AbstractMinecartEntity.Type getMinecartType() {
		return AbstractMinecartEntity.Type.RIDEABLE;
	}

	// ---------- взаимодействие ----------

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (player.shouldCancelInteraction()) {
			return ActionResult.PASS;
		}
		if (this.hasPassengers()) {
			return ActionResult.PASS;
		}
		if (!this.getWorld().isClient) {
			return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void dropItems(DamageSource damageSource) {
		this.kill();
		if (!this.getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			return;
		}
		ItemStack stack = new ItemStack(dev.vagonetki.ModItems.forKind(kind()));
		if (this.hasCustomName()) {
			stack.setCustomName(this.getCustomName());
		}
		this.dropStack(stack);
	}

	@Override
	public ItemStack getPickBlockStack() {
		return new ItemStack(dev.vagonetki.ModItems.forKind(kind()));
	}

	// ---------- управление ----------

	/** Приходит с клиента каждый тик: набор нажатых клавиш (биты F_*). */
	public void setControls(int bits) {
		this.controls = bits & 0x3F;
		this.controlAge = 0;
	}

	private boolean has(int flag) {
		return (controls & flag) != 0;
	}

	public float getHeadingRaw() {
		return this.dataTracker.get(HEADING);
	}

	private void setHeading(float degrees) {
		if (Math.abs(MathHelper.wrapDegrees(degrees - getHeadingRaw())) > 0.01f) {
			this.dataTracker.set(HEADING, degrees);
		}
	}

	/** Для рендера: плавный угол носа между тиками. */
	public float getHeading(float tickDelta) {
		return MathHelper.lerpAngleDegrees(tickDelta, prevRenderHeading, renderHeading);
	}

	// ---------- тик ----------

	@Override
	public void tick() {
		prevRenderHeading = renderHeading;
		if (this.getWorld().isClient) {
			super.tick();
		} else {
			serverTick();
		}
		renderHeading = getHeadingRaw();
		if (!headingInit) {
			headingInit = true;
			prevRenderHeading = renderHeading;
		}
		tickAnimation();
	}

	// ---------- плавная анимация (считается на каждой стороне отдельно, только для вида) ----------

	private float propAngle;
	private float prevPropAngle;
	private float propSpin;
	private float bank;
	private float prevBank;
	private float tilt;
	private float prevTilt;

	private void tickAnimation() {
		prevPropAngle = propAngle;
		prevBank = bank;
		prevTilt = tilt;

		Vec3d v = this.getVelocity();
		double hSpeed = Math.sqrt(v.x * v.x + v.z * v.z);
		float spinTarget = this.hasPassengers() ? (float) (0.5 + Math.min(hSpeed * 1.6, 1.3)) : 0.0f;
		propSpin += (spinTarget - propSpin) * 0.08f;
		propAngle += propSpin;

		float turn = MathHelper.wrapDegrees(renderHeading - prevRenderHeading);
		float bankTarget = MathHelper.clamp(turn * 7.0f, -35.0f, 35.0f);
		bank += (bankTarget - bank) * 0.2f;

		float tiltTarget = MathHelper.clamp((float) (-v.y * 70.0), -25.0f, 25.0f);
		tilt += (tiltTarget - tilt) * 0.2f;
	}

	public float getPropAngle(float tickDelta) {
		return MathHelper.lerp(tickDelta, prevPropAngle, propAngle);
	}

	public float getBank(float tickDelta) {
		return MathHelper.lerp(tickDelta, prevBank, bank);
	}

	public float getTilt(float tickDelta) {
		return MathHelper.lerp(tickDelta, prevTilt, tilt);
	}

	private void serverTick() {
		CartKind k = kind();
		if (++controlAge > 10) {
			controls = 0;
		}
		boolean rider = this.getFirstPassenger() instanceof PlayerEntity;
		if (!rider) {
			armed = false;
			flightMode = false;
			controls = 0;
		} else if (k.needsPoweredRail() && !armed && onMatchingPoweredRail()) {
			armed = true;
		}

		boolean abilities = rider && (!k.needsPoweredRail() || armed);

		if (abilities && k.drives()) {
			driveTick();
			return;
		}

		if (abilities && k.jumps() && has(F_JUMP) && !airborne && (this.isOnGround() || isOnRail())) {
			Vec3d v = this.getVelocity();
			this.setVelocity(v.x, 0.48, v.z);
			this.airborne = true;
			this.playSound(SoundEvents.BLOCK_SLIME_BLOCK_PLACE, 1.0f, 1.0f);
		}

		if (airborne) {
			airTick();
		} else {
			super.tick();
			updateHeadingFromVelocity();
		}
	}

	private boolean isOnRail() {
		World w = this.getWorld();
		int x = MathHelper.floor(this.getX());
		int y = MathHelper.floor(this.getY());
		int z = MathHelper.floor(this.getZ());
		return w.getBlockState(new BlockPos(x, y, z)).isIn(BlockTags.RAILS)
			|| w.getBlockState(new BlockPos(x, y - 1, z)).isIn(BlockTags.RAILS);
	}

	private boolean onMatchingPoweredRail() {
		World w = this.getWorld();
		int x = MathHelper.floor(this.getX());
		int y = MathHelper.floor(this.getY());
		int z = MathHelper.floor(this.getZ());
		for (int dy = 0; dy >= -1; dy--) {
			BlockState s = w.getBlockState(new BlockPos(x, y + dy, z));
			if (s.getBlock() instanceof CartRailBlock rail && rail.supports(kind()) && s.get(PoweredRailBlock.POWERED)) {
				return true;
			}
		}
		return false;
	}

	private void updateHeadingFromVelocity() {
		Vec3d v = this.getVelocity();
		if (v.x * v.x + v.z * v.z > 1.0E-3) {
			setHeading((float) Math.toDegrees(Math.atan2(v.z, v.x)));
		}
	}

	private void tickCommon() {
		if (this.getDamageWobbleTicks() > 0) {
			this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
		}
		if (this.getDamageWobbleStrength() > 0.0f) {
			this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0f);
		}
		if (this.getY() < this.getWorld().getBottomY() - 64) {
			this.discard();
		}
	}

	/** Прыжок слизи: свободный полёт по дуге, на земле отскок, на рельсах возврат к обычной езде. */
	private void airTick() {
		tickCommon();
		Vec3d v = this.getVelocity();
		double vy = (v.y - 0.04) * 0.98;
		this.setVelocity(v.x * 0.99, vy, v.z * 0.99);
		this.move(MovementType.SELF, this.getVelocity());
		this.checkBlockCollision();
		this.fallDistance = 0.0f;
		if (this.isOnGround() && vy < 0) {
			Vec3d nv = this.getVelocity();
			if (kind().jumps && vy < -0.3) {
				this.setVelocity(nv.x, -vy * 0.7, nv.z);
				this.playSound(SoundEvents.BLOCK_SLIME_BLOCK_STEP, 1.0f, 1.0f);
			} else {
				this.airborne = false;
			}
		}
		updateHeadingFromVelocity();
	}

	/** Свободное движение для самолёта, лодки и универсальной вагонетки. */
	private void driveTick() {
		World w = this.getWorld();
		CartKind k = kind();
		boolean fwd = has(F_FORWARD);
		boolean back = has(F_BACK);
		boolean left = has(F_LEFT);
		boolean right = has(F_RIGHT);
		boolean up = has(F_UP);
		tickCommon();

		BlockPos feet = BlockPos.ofFloored(this.getX(), this.getY() + 0.1, this.getZ());
		boolean inWater = w.getFluidState(feet).isIn(FluidTags.WATER);

		if (k.flies && up) {
			flightMode = true;
		} else if (flightMode && !up && (this.isOnGround() || inWater)) {
			flightMode = false;
		}
		if (!k.flies) {
			flightMode = false;
		}

		double heading = getHeadingRaw();
		Vec3d vel = this.getVelocity();
		double rad = Math.toRadians(heading);
		double speed = vel.x * Math.cos(rad) + vel.z * Math.sin(rad);

		// Поворот: D по часовой стрелке (вправо), A против. Чем медленнее, тем меньше руль.
		double turn = (flightMode ? 3.0 : 2.5) * Math.min(1.0, 0.3 + Math.abs(speed) * 2.5);
		if (left) {
			heading -= turn;
		}
		if (right) {
			heading += turn;
		}
		heading = MathHelper.wrapDegrees(heading);
		rad = Math.toRadians(heading);
		double dx = Math.cos(rad);
		double dz = Math.sin(rad);

		// Тяга вперёд / назад
		double accel;
		double maxForward;
		double maxBack;
		double drag;
		if (flightMode) {
			accel = 0.035;
			maxForward = 0.9;
			maxBack = 0.0;
			drag = 0.985;
		} else if (inWater && k.floats) {
			accel = 0.03;
			maxForward = 0.6;
			maxBack = 0.25;
			drag = 0.975;
		} else {
			accel = 0.025;
			maxForward = (k.floats && !k.flies) ? 0.3 : 0.45;
			maxBack = (k == CartKind.PLANE) ? 0.0 : 0.2;
			drag = 0.88;
		}
		if (fwd) {
			speed = speed < maxForward ? Math.min(maxForward, speed + accel) : speed * drag;
		} else if (back && !flightMode && maxBack > 0.0) {
			speed = Math.max(-maxBack, speed - accel);
		} else {
			speed *= drag;
		}

		// Вертикаль
		double vy = vel.y;
		if (flightMode) {
			double target = up ? 0.28 : (back ? -0.28 : (speed < 0.3 ? -0.08 : 0.0));
			vy += (target - vy) * 0.3;
		} else if (inWater && k.floats) {
			FluidState fs = w.getFluidState(feet);
			double surface = feet.getY() + fs.getHeight(w, feet);
			double dy = (surface - 0.2) - this.getY();
			vy = MathHelper.clamp(vy * 0.5 + dy * 0.3, -0.3, 0.3);
		} else {
			vy = (vy - 0.04) * 0.98;
		}

		// Прыжок (универсальная): с земли или с воды
		if (k.jumps && has(F_JUMP) && (this.isOnGround() || (inWater && k.floats)) && !flightMode) {
			vy = 0.5;
			this.playSound(SoundEvents.BLOCK_SLIME_BLOCK_PLACE, 1.0f, 1.0f);
		}

		this.setVelocity(dx * speed, vy, dz * speed);
		this.move(MovementType.SELF, this.getVelocity());
		this.checkBlockCollision();
		this.fallDistance = 0.0f;
		this.setYaw((float) heading);
		setHeading((float) heading);
	}
}
