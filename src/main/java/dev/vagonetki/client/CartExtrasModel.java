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

/** Крылья, хвост, винт и поплавки. Нос смотрит в сторону -X модели (после отражения рендера это «вперёд»). */
public class CartExtrasModel {
	private final ModelPart root;
	private final ModelPart wings;
	private final ModelPart tail;
	private final ModelPart propeller;
	private final ModelPart pontoons;

	public CartExtrasModel(ModelPart root) {
		this.root = root;
		this.wings = root.getChild("wings");
		this.tail = root.getChild("tail");
		this.propeller = root.getChild("propeller");
		this.pontoons = root.getChild("pontoons");
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
		root.addChild("propeller",
			ModelPartBuilder.create()
				.uv(140, 0).cuboid(-0.5f, -7f, -1f, 1f, 14f, 2f)
				.uv(150, 0).cuboid(-0.5f, -1f, -7f, 1f, 2f, 14f)
				.uv(190, 0).cuboid(-1.5f, -1.5f, -1.5f, 3f, 3f, 3f),
			ModelTransform.pivot(-12f, 17f, 0f));
		root.addChild("pontoons",
			ModelPartBuilder.create()
				.uv(0, 90).cuboid(-16f, 19f, 8f, 32f, 4f, 3f)
				.uv(0, 100).cuboid(-16f, 19f, -11f, 32f, 4f, 3f),
			ModelTransform.NONE);
		return TexturedModelData.of(data, 256, 128);
	}

	public void render(CartKind kind, float propAngle, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
		wings.visible = kind.flies;
		tail.visible = kind.flies;
		propeller.visible = kind.flies;
		pontoons.visible = kind.floats;
		propeller.pitch = propAngle;
		root.render(matrices, vertices, light, overlay, 1f, 1f, 1f, 1f);
	}
}
