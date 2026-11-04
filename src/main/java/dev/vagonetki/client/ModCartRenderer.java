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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * Рисует обычный корпус вагонетки с нашей текстурой, сверху детали (крылья, винт, ротор...).
 * Всё плавно: крен на поворотах, наклон носа, бочка и петля, сплющивание слизи, покачивание на воде.
 */
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

	/** Поворачивает вокруг центра вагонетки: tilt вокруг поперечной оси (нос вверх/вниз), roll вокруг продольной. */
	private static void applyAttitude(MatrixStack matrices, float tilt, float roll) {
		matrices.translate(0.0, 0.35, 0.0);
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(tilt));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(roll));
		matrices.translate(0.0, -0.35, 0.0);
	}

	@Override
	public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		CartKind kind = entity.kind();
		float heading = entity.getHeading(tickDelta);
		float time = entity.age + tickDelta;

		float tilt = 0.0f;
		float roll = 0.0f;
		if (kind.flies || kind.hovers || kind.dives) {
			tilt = entity.getTilt(tickDelta);
		}
		if (kind.flies || kind.hovers || kind.glides) {
			roll = entity.getBank(tickDelta) * (kind.glides ? 0.5f : 1.0f);
		}
		if (kind.floatsWater || kind.floatsLava || kind.dives) {
			roll += MathHelper.sin(time * 0.12f) * (entity.hasPassengers() ? 1.8f : 1.0f);
		}
		if (kind.canFlip) {
			float flip = entity.getFlipAngle(tickDelta);
			if (entity.getFlipType() == 1) {
				roll += flip;
			} else if (entity.getFlipType() == 2) {
				tilt += flip;
			}
		}

		// Корпус: поворот в системе носа, затем обратно, чтобы ванильный рендер не сломался.
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-heading));
		applyAttitude(matrices, tilt, roll);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(heading));
		if (kind == CartKind.SLIME) {
			float squash = entity.getSquash(tickDelta);
			matrices.scale(1.0f + squash * 0.5f, 1.0f - squash, 1.0f + squash * 0.5f);
		}
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
		matrices.pop();

		if (!kind.hasExtras()) {
			return;
		}
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-heading));
		applyAttitude(matrices, tilt, roll);
		matrices.scale(-1.0f, -1.0f, 1.0f);
		matrices.translate(0.0, -1.5, 0.0);
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(EXTRAS_TEXTURE));
		extras.render(kind, entity.getPropAngle(tickDelta), time, matrices, consumer, light, OverlayTexture.DEFAULT_UV);
		matrices.pop();
	}
}
