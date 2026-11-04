package dev.vagonetki.client;

import dev.vagonetki.Vagonetki;
import dev.vagonetki.entity.CartKind;
import dev.vagonetki.entity.ModCartEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/** Рисует обычный корпус вагонетки с нашей текстурой, а сверху крылья, винт и поплавки с плавным креном. */
public class ModCartRenderer<T extends ModCartEntity> extends MinecartEntityRenderer<T> {
	private static final Identifier EXTRAS_TEXTURE = Vagonetki.id("textures/entity/cart_extras.png");

	private final CartExtrasModel extras;

	public ModCartRenderer(EntityRendererFactory.Context ctx) {
		super(ctx, EntityModelLayers.MINECART);
		this.extras = new CartExtrasModel(ctx.getPart(VagonetkiClient.EXTRAS_LAYER));
	}

	@Override
	public Identifier getTexture(T entity) {
		return entity.kind().bodyTexture();
	}

	@Override
	public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
		CartKind kind = entity.kind();
		if (!kind.hasExtras()) {
			return;
		}
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-entity.getHeading(tickDelta)));
		if (kind.flies) {
			// Наклон носа вверх/вниз и крен на поворотах, всё сглажено между тиками.
			matrices.translate(0.0, 0.35, 0.0);
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(entity.getTilt(tickDelta)));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getBank(tickDelta)));
			matrices.translate(0.0, -0.35, 0.0);
		}
		matrices.scale(-1.0f, -1.0f, 1.0f);
		matrices.translate(0.0, -1.5, 0.0);
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(EXTRAS_TEXTURE));
		extras.render(kind, entity.getPropAngle(tickDelta), matrices, consumer, light, OverlayTexture.DEFAULT_UV);
		matrices.pop();
	}
}
