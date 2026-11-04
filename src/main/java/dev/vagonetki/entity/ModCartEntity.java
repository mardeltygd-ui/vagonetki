package dev.vagonetki.entity;

import dev.vagonetki.ModItems;
import dev.vagonetki.block.CartRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
 * пока игрок не выйдет. Слизь-вагонетка не требует рельсов особого цвета и только прыгает.
 */
public abstract class ModCartEntity extends AbstractMinecartEntity {
	public static final int F_FORWARD = 1;
	public static final int F_BACK = 2;
	public static final int F_LEFT = 4;
	public static final int F_RIGHT = 8;
	public static final int F_UP = 16;
	public static final int F_JUMP = 32;
	public static final int F_ROLL = 64;
	public static final int F_LOOP = 128;

	public static final int ROLL_TICKS = 24;
	public static final int LOOP_TICKS = 36;
	public static final int SLIME_MAX_CHARGE = 14;
	private static final int LAND_TICKS = 6;

	/** Куда смотрит нос вагонетки, в градусах: вперёд = (cos, sin) по осям X и Z. */
	private static final TrackedData<Float> HEADING = DataTracker.registerData(ModCartEntity.class, TrackedDataHandlerRegistry.FLOAT);
	/** 0 нет переворота, 1 бочка, 2 петля. */
	private static final TrackedData<Integer> FLIP_TYPE = DataTracker.registerData(ModCartEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> FLIP_TICK = DataTracker.registerData(ModCartEntity.class, TrackedDataHandlerRegistry.INTEGER);
	/** Заряд прыжка слизи, 0..SLIME_MAX_CHARGE. */
	private static final TrackedData<Integer> CHARGE = DataTracker.registerData(ModCartEntity.class, TrackedDataHandlerRegistry.INTEGER);
	/** Счётчик «приземления» для анимации сплющивания. */
	private static final TrackedData<Integer> LAND = DataTracker.registerData(ModCartEntity.class, TrackedDataHandlerRegistry.INTEGER);

	private int controls;
	private int controlAge;
	private boolean armed;
	private boolean flightMode;
	private boolean airborne;
	private int flipCooldown;
	private int blinkCooldown;

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
		this.dataTracker.startTracking(FLIP_TYPE, 0);
		this.dataTracker.startTracking(FLIP_TICK, 0);
		this.dataTracker.startTracking(CHARGE, 0);
		this.dataTracker.startTracking(LAND, 0);
	}

	@Override
	public AbstractMinecartEntity.Type getMinecartType() {
		return AbstractMinecartEntity.Type.RIDEABLE;
	}

	@Override
	public boolean isFireImmune() {
		return kind() == CartKind.LAVA || super.isFireImmune();
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
		ItemStack stack = new ItemStack(ModItems.forKind(kind()));
		if (this.hasCustomName()) {
			stack.setCustomName(this.getCustomName());
		}
		this.dropStack(stack);
	}

	@Override
	public Item getItem() {
		return ModItems.forKind(kind());
	}

	@Override
	public ItemStack getPickBlockStack() {
		return new ItemStack(ModItems.forKind(kind()));
	}

	// ---------- управление ----------

	/** Приходит с клиента каждый тик: набор нажатых клавиш (биты F_*). */
	public void setControls(int bits) {
		this.controls = bits & 0xFF;
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

	private void setFlip(int type, int tick) {
		if (this.dataTracker.get(FLIP_TYPE) != type) {
			this.dataTracker.set(FLIP_TYPE, type);
		}
		if (this.dataTracker.get(FLIP_TICK) != tick) {
			this.dataTracker.set(FLIP_TICK, tick);
		}
	}

	private void setCharge(int value) {
		if (this.dataTracker.get(CHARGE) != value) {
			this.dataTracker.set(CHARGE, value);
		}
	}

	private void setLand(int value) {
		if (this.dataTracker.get(LAND) != value) {
			this.dataTracker.set(LAND, value);
		}
	}

	// ---------- эффекты и звуки (только сервер) ----------

	private void snd(SoundEvent sound, float volume, float pitch) {
		this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundCategory.NEUTRAL, volume, pitch);
	}

	/** Частицы позади (back), сбоку (side) и выше (lift) вагонетки относительно её носа. */
	private void puff(ParticleEffect effect, double back, double side, double lift, int count, double spread, double speed) {
		if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
			return;
		}
		double rad = Math.toRadians(getHeadingRaw());
		double dx = Math.cos(rad);
		double dz = Math.sin(rad);
		double px = this.getX() - dx * back - dz * side;
		double pz = this.getZ() - dz * back + dx * side;
		serverWorld.spawnParticles(effect, px, this.getY() + lift, pz, count, spread, spread, spread, speed);
	}

	// ---------- для рендера (плавные значения между тиками) ----------

	/** Угол носа между тиками. */
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
	private float flipAngle;
	private float prevFlipAngle;
	private float squash;
	private float prevSquash;

	private void tickAnimation() {
		prevPropAngle = propAngle;
		prevBank = bank;
		prevTilt = tilt;
		prevFlipAngle = flipAngle;
		prevSquash = squash;

		Vec3d v = this.getVelocity();
		double hSpeed = Math.sqrt(v.x * v.x + v.z * v.z);

		float spinTarget = this.hasPassengers() ? (float) (0.5 + Math.min(hSpeed * 1.6, 1.3)) : 0.0f;
		propSpin += (spinTarget - propSpin) * 0.08f;
		propAngle += propSpin;

		float turn = MathHelper.wrapDegrees(renderHeading - prevRenderHeading);
		float bankTarget = MathHelper.clamp(turn * 7.0f, -35.0f, 35.0f);
		bank += (bankTarget - bank) * 0.2f;

		float tiltTarget = MathHelper.clamp((float) (v.y * 70.0), -25.0f, 25.0f);
		tilt += (tiltTarget - tilt) * 0.2f;

		int flipType = this.dataTracker.get(FLIP_TYPE);
		float flipTarget = 0.0f;
		if (flipType != 0) {
			int dur = flipType == 1 ? ROLL_TICKS : LOOP_TICKS;
			flipTarget = (this.dataTracker.get(FLIP_TICK) + 1) / (float) dur * 360.0f;
		}
		if (flipTarget < flipAngle - 180.0f) {
			// переворот закончился: 360 -> 0 без обратного прокручивания
			prevFlipAngle -= 360.0f;
		}
		flipAngle = flipTarget;

		int charge = this.dataTracker.get(CHARGE);
		int land = this.dataTracker.get(LAND);
		float stretch = MathHelper.clamp((float) (v.y * 0.4), -0.25f, 0.35f);
		float squashTarget = charge / (float) SLIME_MAX_CHARGE * 0.35f + land / (float) LAND_TICKS * 0.28f - stretch;
		squash += (squashTarget - squash) * 0.35f;
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

	/** Угол переворота в градусах: 0..360, активен только пока идёт бочка или петля. */
	public float getFlipAngle(float tickDelta) {
		return MathHelper.lerp(tickDelta, prevFlipAngle, flipAngle);
	}

	public int getFlipType() {
		return this.dataTracker.get(FLIP_TYPE);
	}

	/** Положительное значение: сплющивание (заряд, приземление), отрицательное: вытягивание в прыжке. */
	public float getSquash(float tickDelta) {
		return MathHelper.lerp(tickDelta, prevSquash, squash);
	}

	// ---------- серверная логика ----------

	private void serverTick() {
		CartKind k = kind();
		if (++controlAge > 10) {
			controls = 0;
		}
		int land = this.dataTracker.get(LAND);
		if (land > 0) {
			setLand(land - 1);
		}

		boolean rider = this.getFirstPassenger() instanceof PlayerEntity;
		if (!rider) {
			armed = false;
			flightMode = false;
			controls = 0;
			setFlip(0, 0);
			setCharge(0);
		} else if (k.needsPoweredRail() && !armed && onMatchingPoweredRail()) {
			armed = true;
			snd(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
			snd(SoundEvents.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);
			puff(ParticleTypes.END_ROD, 0.0, 0.0, 0.6, 24, 0.5, 0.05);
		}

		boolean abilities = rider && (!k.needsPoweredRail() || armed);

		if (abilities && k.drives()) {
			driveTick();
			return;
		}

		if (k == CartKind.SLIME) {
			slimeTick(rider);
			return;
		}

		super.tick();
		updateHeadingFromVelocity();
	}

	private boolean isOnAnyRail() {
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

	// ---------- слизь: прыжок с зарядом ----------

	/**
	 * Зажми пробел: слизь приседает и копит заряд (до ~0,7 секунды). Отпусти: прыжок, тем выше, чем дольше держал.
	 * В воздухе слизь не управляется. При падении она отскакивает, пока удар сильный.
	 */
	private void slimeTick(boolean rider) {
		boolean onSurface = this.isOnGround() || isOnAnyRail();
		int charge = this.dataTracker.get(CHARGE);

		if (rider && !airborne && onSurface) {
			if (has(F_JUMP)) {
				if (charge < SLIME_MAX_CHARGE) {
					charge++;
					if (charge % 4 == 1) {
						snd(SoundEvents.BLOCK_SLIME_BLOCK_HIT, 0.5f, 0.7f + charge * 0.04f);
					}
				}
				setCharge(charge);
			} else if (charge > 0) {
				double power = 0.40 + 0.0215 * charge;
				Vec3d v = this.getVelocity();
				this.setVelocity(v.x, power, v.z);
				this.airborne = true;
				setCharge(0);
				snd(SoundEvents.BLOCK_SLIME_BLOCK_PLACE, 1.0f, 1.4f - charge * 0.03f);
				puff(ParticleTypes.ITEM_SLIME, 0.0, 0.0, 0.1, 14, 0.35, 0.1);
			}
		} else if (charge != 0) {
			setCharge(0);
		}

		if (airborne) {
			airTick();
		} else {
			super.tick();
			updateHeadingFromVelocity();
		}
	}

	/** Прыжок слизи: свободный полёт по дуге, на земле отскок, на рельсах возврат к обычной езде. */
	private void airTick() {
		tickCommon();
		Vec3d v = this.getVelocity();
		double vy = (v.y - 0.04) * 0.98;
		this.setVelocity(v.x * 0.995, vy, v.z * 0.995);
		this.move(MovementType.SELF, this.getVelocity());
		this.checkBlockCollision();
		this.fallDistance = 0.0f;

		if (this.age % 3 == 0 && Math.abs(vy) > 0.2) {
			puff(ParticleTypes.ITEM_SLIME, 0.0, 0.0, 0.2, 1, 0.2, 0.0);
		}

		if (this.isOnGround() && vy < 0) {
			Vec3d nv = this.getVelocity();
			setLand(LAND_TICKS);
			puff(ParticleTypes.ITEM_SLIME, 0.0, 0.0, 0.1, 12, 0.4, 0.15);
			if (vy < -0.3) {
				this.setVelocity(nv.x, -vy * 0.72, nv.z);
				snd(SoundEvents.BLOCK_SLIME_BLOCK_FALL, 0.9f, 1.0f + (float) vy * 0.3f);
			} else {
				this.airborne = false;
				snd(SoundEvents.BLOCK_SLIME_BLOCK_STEP, 0.7f, 1.0f);
			}
		}
		updateHeadingFromVelocity();
	}

	// ---------- свободное движение: самолёт, лодка, универсальная, вертолёт, подлодка, лёд, лава, эндер, ракета ----------

	private void driveTick() {
		World w = this.getWorld();
		CartKind k = kind();
		boolean fwd = has(F_FORWARD);
		boolean back = has(F_BACK);
		boolean left = has(F_LEFT);
		boolean right = has(F_RIGHT);
		boolean up = has(F_UP);
		tickCommon();
		if (flipCooldown > 0) {
			flipCooldown--;
		}
		if (blinkCooldown > 0) {
			blinkCooldown--;
		}

		BlockPos feet = BlockPos.ofFloored(this.getX(), this.getY() + 0.1, this.getZ());
		FluidState fs = w.getFluidState(feet);
		boolean inWater = fs.isIn(FluidTags.WATER);
		boolean inLava = fs.isIn(FluidTags.LAVA);
		boolean surface = (k.floatsWater && inWater) || (k.floatsLava && (inLava || inWater));
		boolean submerged = k.dives && inWater;

		if (k.floatsLava && this.age % 20 == 0 && this.getFirstPassenger() instanceof LivingEntity rider) {
			rider.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 80, 0, false, false, false));
		}

		double heading = getHeadingRaw();
		Vec3d vel = this.getVelocity();
		double rad = Math.toRadians(heading);
		double speed = vel.x * Math.cos(rad) + vel.z * Math.sin(rad);

		// Режим полёта
		boolean canFly = k.flies || k.hovers;
		if (canFly && up) {
			flightMode = true;
		} else if (k == CartKind.ROCKET && speed > 0.55) {
			flightMode = true;
		} else if (flightMode && !up && (this.isOnGround() || inWater || inLava) && !(k == CartKind.ROCKET && speed > 0.55)) {
			flightMode = false;
		}
		if (!canFly) {
			flightMode = false;
		}

		// Перевороты самолёта: R бочка, G петля
		int flipType = this.dataTracker.get(FLIP_TYPE);
		if (flipType != 0) {
			int t = this.dataTracker.get(FLIP_TICK) + 1;
			int dur = flipType == 1 ? ROLL_TICKS : LOOP_TICKS;
			if (t >= dur) {
				setFlip(0, 0);
				flipCooldown = 25;
				flipType = 0;
			} else {
				setFlip(flipType, t);
			}
		} else if (k.canFlip && flightMode && flipCooldown == 0 && speed > 0.35 && (has(F_ROLL) || has(F_LOOP))) {
			flipType = has(F_ROLL) ? 1 : 2;
			setFlip(flipType, 0);
			speed += 0.1;
			snd(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.6f, 1.3f);
			snd(SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.7f, 1.1f);
			puff(ParticleTypes.FIREWORK, 0.0, 0.0, 0.7, 16, 0.5, 0.08);
		}
		boolean flipping = flipType != 0;
		int flipTick = this.dataTracker.get(FLIP_TICK);

		// Поворот: D по часовой стрелке (вправо), A против. Чем медленнее, тем меньше руль.
		if (!flipping) {
			double turn = (flightMode ? 3.0 : 2.5) * Math.min(1.0, 0.3 + Math.abs(speed) * 2.5);
			if (k.glides) {
				turn *= 0.6;
			}
			if (left) {
				heading -= turn;
			}
			if (right) {
				heading += turn;
			}
		}
		heading = MathHelper.wrapDegrees(heading);
		rad = Math.toRadians(heading);
		double dx = Math.cos(rad);
		double dz = Math.sin(rad);

		// Эндер: пробел телепортирует вперёд до 7 блоков
		if (k.blinks && has(F_JUMP) && blinkCooldown == 0) {
			double best = 0.0;
			for (double d = 0.5; d <= 7.0; d += 0.5) {
				Box moved = this.getBoundingBox().offset(dx * d, 0.0, dz * d);
				if (!w.isSpaceEmpty(this, moved)) {
					break;
				}
				best = d;
			}
			if (best >= 1.0) {
				puff(ParticleTypes.PORTAL, 0.0, 0.0, 0.6, 30, 0.45, 0.6);
				snd(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
				this.setPosition(this.getX() + dx * best, this.getY(), this.getZ() + dz * best);
				puff(ParticleTypes.PORTAL, 0.0, 0.0, 0.6, 30, 0.45, 0.6);
				snd(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.9f);
				blinkCooldown = 30;
			}
		}

		// Тяга вперёд / назад
		double accel;
		double maxForward;
		double maxBack;
		double drag;
		if (flightMode) {
			if (k == CartKind.ROCKET) {
				accel = 0.06;
				maxForward = 1.5;
				drag = 0.99;
			} else if (k.hovers) {
				accel = 0.03;
				maxForward = 0.5;
				drag = 0.95;
			} else {
				accel = 0.035;
				maxForward = 0.9;
				drag = 0.985;
			}
			maxBack = 0.0;
		} else if (submerged) {
			accel = 0.03;
			maxForward = 0.55;
			maxBack = 0.0;
			drag = 0.97;
		} else if (surface) {
			accel = 0.03;
			maxForward = 0.6;
			maxBack = 0.25;
			drag = 0.975;
		} else if (k.glides) {
			accel = 0.03;
			maxForward = 0.8;
			maxBack = 0.2;
			drag = 0.992;
		} else {
			accel = 0.025;
			maxForward = (k.floatsWater && !k.flies) ? 0.3 : 0.45;
			maxBack = (k == CartKind.PLANE || k == CartKind.ROCKET || k == CartKind.HELICOPTER || k == CartKind.SUBMARINE) ? 0.0 : 0.2;
			drag = 0.88;
		}
		if (flipping) {
			// во время переворота скорость не теряется
		} else if (fwd) {
			speed = speed < maxForward ? Math.min(maxForward, speed + accel) : speed * drag;
		} else if (back && !flightMode && !submerged && maxBack > 0.0) {
			speed = Math.max(-maxBack, speed - accel);
		} else {
			speed *= drag;
		}

		// Вертикаль
		double vy = vel.y;
		if (flipping && flipType == 2) {
			double progress = flipTick / (double) LOOP_TICKS;
			vy = 0.55 * Math.sin(2.0 * Math.PI * progress);
		} else if (flightMode) {
			double sink = k.hovers ? 0.0 : (k == CartKind.ROCKET ? -0.12 : -0.08);
			double target = up ? 0.28 : (back ? -0.28 : (speed < 0.3 ? sink : 0.0));
			vy += (target - vy) * 0.3;
		} else if (submerged) {
			double target = up ? 0.2 : (back ? -0.2 : 0.0);
			vy += (target - vy) * 0.2;
		} else if (surface) {
			double top = feet.getY() + fs.getHeight(w, feet);
			double dy = (top - 0.2) - this.getY();
			vy = MathHelper.clamp(vy * 0.5 + dy * 0.3, -0.3, 0.3);
		} else {
			vy = (vy - 0.04) * 0.98;
		}

		// Универсальная: прыжок с земли или с воды
		if (k == CartKind.UNIVERSAL && has(F_JUMP) && (this.isOnGround() || surface) && !flightMode) {
			vy = 0.55;
			snd(SoundEvents.BLOCK_SLIME_BLOCK_PLACE, 0.9f, 1.2f);
			puff(ParticleTypes.ITEM_SLIME, 0.0, 0.0, 0.1, 10, 0.35, 0.1);
		}

		this.setVelocity(dx * speed, vy, dz * speed);
		this.move(MovementType.SELF, this.getVelocity());
		this.checkBlockCollision();
		this.fallDistance = 0.0f;
		this.setYaw((float) heading);
		setHeading((float) heading);

		driveEffects(k, speed, fwd, flipping, surface, submerged);
	}

	/** Частицы и звуки движения. Всё мягкое и негромкое. */
	private void driveEffects(CartKind k, double speed, boolean fwd, boolean flipping, boolean surface, boolean submerged) {
		int t = this.age;
		double sp = Math.abs(speed);

		switch (k) {
			case PLANE, UNIVERSAL -> {
				if (flightMode && sp > 0.25) {
					if (t % 2 == 0 || flipping) {
						puff(ParticleTypes.CLOUD, 0.2, 1.7, 0.75, 1, 0.0, 0.0);
						puff(ParticleTypes.CLOUD, 0.2, -1.7, 0.75, 1, 0.0, 0.0);
					}
					if (flipping) {
						puff(ParticleTypes.FIREWORK, 0.4, 0.0, 0.7, 1, 0.15, 0.02);
					}
					if (t % 7 == 0) {
						snd(SoundEvents.ENTITY_PHANTOM_FLAP, 0.25f, 1.7f + (float) sp * 0.4f);
					}
				}
				if (k == CartKind.UNIVERSAL) {
					wake(surface, sp, t, ParticleTypes.SPLASH, SoundEvents.ENTITY_BOAT_PADDLE_WATER);
				}
			}
			case BOAT -> wake(surface, sp, t, ParticleTypes.SPLASH, SoundEvents.ENTITY_BOAT_PADDLE_WATER);
			case LAVA -> {
				if (surface && sp > 0.08) {
					puff(ParticleTypes.LAVA, 0.8, 0.0, 0.3, 1, 0.3, 0.0);
					puff(ParticleTypes.FLAME, 0.9, 0.0, 0.3, 2, 0.2, 0.01);
					if (t % 20 == 0) {
						snd(SoundEvents.BLOCK_LAVA_AMBIENT, 0.3f, 1.0f);
					}
				}
				if (t % 4 == 0) {
					puff(ParticleTypes.FLAME, 0.0, 0.0, 0.9, 1, 0.25, 0.0);
				}
			}
			case HELICOPTER -> {
				if (flightMode) {
					if (t % 3 == 0) {
						snd(SoundEvents.ENTITY_PHANTOM_FLAP, 0.2f, 1.9f);
					}
					if (t % 4 == 0) {
						puff(ParticleTypes.CLOUD, 0.0, 0.0, 0.0, 1, 0.5, 0.0);
					}
				}
			}
			case SUBMARINE -> {
				if (submerged) {
					if (sp > 0.05 && t % 2 == 0) {
						puff(ParticleTypes.BUBBLE, 1.0, 0.0, 0.5, 2, 0.2, 0.02);
					}
					if (t % 14 == 0) {
						snd(SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 0.4f, 1.0f);
					}
					if (t % 60 == 0) {
						snd(SoundEvents.BLOCK_CONDUIT_AMBIENT, 0.4f, 1.0f);
					}
				}
			}
			case ICE -> {
				if (sp > 0.25) {
					puff(ParticleTypes.SNOWFLAKE, 0.9, 0.0, 0.2, 3, 0.25, 0.02);
					if (t % 6 == 0) {
						snd(SoundEvents.BLOCK_POWDER_SNOW_STEP, 0.3f, 1.4f);
					}
				}
			}
			case ENDER -> {
				if (t % 2 == 0) {
					puff(ParticleTypes.PORTAL, 0.0, 0.0, 0.8, 2, 0.4, 0.3);
				}
			}
			case ROCKET -> {
				if (sp > 0.12 && (fwd || flightMode)) {
					puff(ParticleTypes.FLAME, 1.1, 0.0, 0.35, 2, 0.12, 0.02);
					if (t % 2 == 0) {
						puff(ParticleTypes.LARGE_SMOKE, 1.3, 0.0, 0.35, 1, 0.15, 0.02);
					}
					if (t % 5 == 0) {
						snd(SoundEvents.BLOCK_FIRE_AMBIENT, 0.35f, 1.4f);
					}
				}
				if (flightMode && sp > 0.9 && t % 12 == 0) {
					snd(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 1.3f);
				}
			}
			default -> {
			}
		}
	}

	private void wake(boolean surface, double sp, int t, ParticleEffect effect, SoundEvent sound) {
		if (surface && sp > 0.08) {
			puff(effect, 0.9, 0.5, 0.2, 2, 0.15, 0.1);
			puff(effect, 0.9, -0.5, 0.2, 2, 0.15, 0.1);
			if (t % 8 == 0) {
				snd(sound, 0.35f, 1.0f + (float) sp * 0.3f);
			}
		}
	}
}
