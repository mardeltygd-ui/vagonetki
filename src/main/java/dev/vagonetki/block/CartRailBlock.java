package dev.vagonetki.block;

import dev.vagonetki.entity.CartKind;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Цветной рельс. Ведёт себя как обычный рельс (повороты не делает, как и энергорельс),
 * но включается не сигналом, а соседними блоками редстоуна: нужное их количество вплотную к рельсу.
 */
public class CartRailBlock extends PoweredRailBlock {
	private final CartKind kind;
	private final int redstoneBlocksNeeded;

	public CartRailBlock(Settings settings, CartKind kind, int redstoneBlocksNeeded) {
		super(settings);
		this.kind = kind;
		this.redstoneBlocksNeeded = redstoneBlocksNeeded;
	}

	public boolean supports(CartKind other) {
		return other == kind;
	}

	private int countRedstoneBlocks(World world, BlockPos pos) {
		int count = 0;
		for (Direction dir : Direction.values()) {
			if (world.getBlockState(pos.offset(dir)).isOf(Blocks.REDSTONE_BLOCK)) {
				count++;
			}
		}
		return count;
	}

	@Override
	protected void updateBlockState(BlockState state, World world, BlockPos pos, Block neighbor) {
		boolean wasPowered = state.get(POWERED);
		boolean nowPowered = countRedstoneBlocks(world, pos) >= redstoneBlocksNeeded;
		if (nowPowered != wasPowered) {
			world.setBlockState(pos, state.with(POWERED, nowPowered), Block.NOTIFY_ALL);
			world.updateNeighborsAlways(pos.down(), this);
			if (state.get(getShapeProperty()).isAscending()) {
				world.updateNeighborsAlways(pos.up(), this);
			}
		}
	}
}
