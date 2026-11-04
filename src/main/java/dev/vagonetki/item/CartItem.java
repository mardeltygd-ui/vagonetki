package dev.vagonetki.item;

import dev.vagonetki.ModEntities;
import dev.vagonetki.entity.CartKind;
import dev.vagonetki.entity.ModCartEntity;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Предмет-вагонетка: ставится на рельсы так же, как обычная вагонетка. */
public class CartItem extends Item {
	private final CartKind kind;

	public CartItem(CartKind kind, Settings settings) {
		super(settings);
		this.kind = kind;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);
		if (!state.isIn(BlockTags.RAILS)) {
			return ActionResult.FAIL;
		}
		if (!kind.acceptsRail(state)) {
			PlayerEntity player = context.getPlayer();
			if (player != null && !world.isClient) {
				player.sendMessage(Text.translatable("message.vagonetki.wrong_rail"), true);
			}
			return ActionResult.FAIL;
		}
		ItemStack stack = context.getStack();
		if (!world.isClient) {
			RailShape shape = state.getBlock() instanceof AbstractRailBlock rail
				? state.get(rail.getShapeProperty())
				: RailShape.NORTH_SOUTH;
			double lift = shape.isAscending() ? 0.5 : 0.0;
			ModCartEntity cart = ModEntities.create(kind, world, pos.getX() + 0.5, pos.getY() + 0.0625 + lift, pos.getZ() + 0.5);
			if (stack.hasCustomName()) {
				cart.setCustomName(stack.getName());
			}
			world.spawnEntity(cart);
		}
		stack.decrement(1);
		return ActionResult.success(world.isClient);
	}
}
