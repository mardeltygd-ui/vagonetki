package dev.vagonetki.client;

import dev.vagonetki.entity.CartKind;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * Все дополнительные детали вагонеток: крылья, хвост, винты, поплавки, ротор, перископ, лыжи, нос ракеты, кристалл.
 * Нос смотрит в сторону -X модели (после отражения рендера это «вперёд»). Текстура 256x256 общая.
 */
public class CartExtrasModel {
	private final ModelPart root;
	private final ModelPart wings;
	private final ModelPart tail;
	private final ModelPart propeller;
	private final ModelPart rearProp;
	private final ModelPart pontoons;
	private final ModelPart lavaHull;
	private final ModelPart rotor;
	private final ModelPart mast;
	private final ModelPart periscope;
	private final ModelPart tower;
	private final ModelPart skis;
	private final ModelPart nose;
	private final ModelPart nozzle;
	private final ModelPart gem;

	public CartExtrasModel(ModelPart root) {
		this.root = root;
		this.wings = root.getChild("wings");
		this.tail = root.getChild("tail");
		this.propeller = root.getChild("propeller");
		this.rearProp = root.getChild("rear_prop");
		this.pontoons = root.getChild("pontoons");
		this.lavaHull = root.getChild("lava_hull");
		this.rotor = root.getChild("rotor");
		this.mast = root.getChild("mast");
		this.periscope = root.getChild("periscope");
		this.tower = root.getChild("tower");
		this.skis = root.getChild("skis");
		this.nose = root.getChild("nose");
		this.nozzle = root.getChild("nozzle");
		this.gem = root.getChild("gem");
	}

	private static ModelPartBuilder blades() {
		return ModelPartBuilder.create()
			.uv(140, 0).cuboid(-0.5f, -7f, -1f, 1f, 14f, 2f)
			.uv(150, 0).cuboid(-0.5f, -1f, -7f, 1f, 2f, 14f)
			.uv(190, 0).cuboid(-1.5f, -1.5f, -1.5f, 3f, 3f, 3f);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("wings",
			ModelPartBuilder.create().uv(0, 0).cuboid(-6f, 12f, -28f, 12f, 1f, 56f),
			ModelTransform.NONE);
		root.addChild("tail",
			ModelPartBuilder.create()
				.uv(0, 60).cuboid(10f, 4f, -0.5f, 4f, 9f, 1f)
				.uv(0, 72).cuboid(10f, 11f, -7f, 4f, 1f, 14f),
			ModelTransform.NONE);
		root.addChild("propeller", blades(), ModelTransform.pivot(-12f, 17f, 0f));
		root.addChild("rear_prop", blades(), ModelTransform.pivot(13f, 17f, 0f));
		root.addChild("pontoons",
			ModelPartBuilder.create()
				.uv(0, 90).cuboid(-16f, 19f, 8f, 32f, 4f, 3f)
				.uv(0, 100).cuboid(-16f, 19f, -11f, 32f, 4f, 3f),
			ModelTransform.NONE);
		root.addChild("lava_hull",
			ModelPartBuilder.create()
				.uv(0, 110).cuboid(-16f, 19f, 8f, 32f, 4f, 3f)
				.uv(0, 120).cuboid(-16f, 19f, -11f, 32f, 4f, 3f),
			ModelTransform.NONE);
		root.addChild("rotor",
			ModelPartBuilder.create()
				.uv(0, 130).cuboid(-22f, -1f, -1.5f, 44f, 1f, 3f)
				.uv(100, 130).cuboid(-1.5f, -1f, -22f, 3f, 1f, 44f),
			ModelTransform.pivot(0f, 8f, 0f));
		root.addChild("mast",
			ModelPartBuilder.create().uv(140, 30).cuboid(-1f, 8f, -1f, 2f, 5f, 2f),
			ModelTransform.NONE);
		root.addChild("periscope",
			ModelPartBuilder.create().uv(200, 30).cuboid(5f, 0f, -1f, 2f, 13f, 2f),
			ModelTransform.NONE);
		root.addChild("tower",
			ModelPartBuilder.create().uv(0, 180).cuboid(2f, 10f, -4f, 8f, 3f, 8f),
			ModelTransform.NONE);
		root.addChild("skis",
			ModelPartBuilder.create()
				.uv(0, 200).cuboid(-18f, 22f, 7f, 36f, 2f, 3f)
				.uv(0, 210).cuboid(-18f, 22f, -10f, 36f, 2f, 3f),
			ModelTransform.NONE);
		root.addChild("nose",
			ModelPartBuilder.create().uv(100, 180).cuboid(-18f, 13f, -4f, 4f, 6f, 8f),
			ModelTransform.NONE);
		root.addChild("nozzle",
			ModelPartBuilder.create().uv(140, 180).cuboid(11f, 14f, -3f, 3f, 5f, 6f),
			ModelTransform.NONE);
		root.addChild("gem",
			ModelPartBuilder.create().uv(200, 180).cuboid(-3f, -3f, -3f, 6f, 6f, 6f),
			ModelTransform.pivot(0f, 3f, 0f));
		return TexturedModelData.of(data, 256, 256);
	}

	/** @param spin угол винта/ротора в градусах (плавный, копится по тикам), @param time время для покачивания */
	public void render(CartKind kind, float spin, float time, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
		boolean plane = kind == CartKind.PLANE || kind == CartKind.UNIVERSAL;
		wings.visible = plane;
		tail.visible = plane || kind == CartKind.ROCKET || kind == CartKind.HELICOPTER;
		propeller.visible = plane;
		rearProp.visible = kind == CartKind.SUBMARINE;
		pontoons.visible = kind == CartKind.BOAT || kind == CartKind.UNIVERSAL;
		lavaHull.visible = kind == CartKind.LAVA;
		rotor.visible = kind == CartKind.HELICOPTER;
		mast.visible = kind == CartKind.HELICOPTER;
		periscope.visible = kind == CartKind.SUBMARINE;
		tower.visible = kind == CartKind.SUBMARINE;
		skis.visible = kind == CartKind.ICE;
		nose.visible = kind == CartKind.ROCKET;
		nozzle.visible = kind == CartKind.ROCKET;
		gem.visible = kind == CartKind.ENDER;

		propeller.pitch = spin;
		rearProp.pitch = -spin * 1.2f;
		rotor.yaw = spin * 0.9f;
		gem.yaw = spin * 0.12f;
		gem.pivotY = 3.0f + MathHelper.sin(time * 0.1f) * 1.5f;

		root.render(matrices, vertices, light, overlay, 1f, 1f, 1f, 1f);
	}
}
