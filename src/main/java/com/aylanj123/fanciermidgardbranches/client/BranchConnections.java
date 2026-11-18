package com.aylanj123.fanciermidgardbranches.client;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.common.Tags;

final class BranchConnections {
    private static final Direction[] SIDES = {
        Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };
    private static final BooleanProperty[] CONNECTIONS = {
        BlockStateProperties.NORTH, BlockStateProperties.EAST,
        BlockStateProperties.SOUTH, BlockStateProperties.WEST
    };
    private static final List<TagKey<Block>> GROUND_TAGS = List.of(
        BlockTags.DIRT, BlockTags.SAND, BlockTags.BASE_STONE_OVERWORLD,
        BlockTags.BASE_STONE_NETHER, Tags.Blocks.STONES, Tags.Blocks.GRAVELS,
        Tags.Blocks.SANDS
    );

    private static boolean isBranch(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id.getNamespace().equals("midgard") && id.getPath().endsWith("_branch");
    }

    private static boolean isLeaves(BlockState state) {
        return state.getBlock() instanceof LeavesBlock || state.is(BlockTags.LEAVES);
    }

    private static boolean isWood(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return state.is(BlockTags.LOGS)
            || id.getNamespace().equals("midgard") && id.getPath().endsWith("_trunk");
    }

    private static boolean isGround(BlockState state) {
        for (TagKey<Block> tag : GROUND_TAGS) {
            if (state.is(tag)) {
                return true;
            }
        }
        return state.is(Blocks.CLAY) || state.is(Blocks.CALCITE)
            || state.is(Blocks.DRIPSTONE_BLOCK) || state.is(Blocks.END_STONE)
            || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)
            || state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT_PATH);
    }

    private static boolean connects(BlockState state) {
        return isBranch(state) || isLeaves(state) || isWood(state) || isGround(state);
    }

    private static int horizontalConnections(BlockAndTintGetter level, BlockPos position, BlockState state) {
        int result = 0;
        for (int side = 0; side < SIDES.length; side++) {
            BlockState neighbor = level.getBlockState(position.relative(SIDES[side]));
            if (connects(neighbor) || !neighbor.isAir()
                    && state.hasProperty(CONNECTIONS[side]) && state.getValue(CONNECTIONS[side])) {
                result |= 1 << side;
            }
        }
        return result;
    }

    private static boolean hasHorizontalBranch(BlockAndTintGetter level, BlockPos position) {
        for (Direction side : SIDES) {
            if (isBranch(level.getBlockState(position.relative(side)))) {
                return true;
            }
        }
        return false;
    }

    static int resolve(BlockAndTintGetter level, BlockPos position, BlockState state, int variants) {
        int horizontal = horizontalConnections(level, position, state);
        BlockState above = level.getBlockState(position.above());
        BlockState below = level.getBlockState(position.below());
        boolean horizontalBranch = hasHorizontalBranch(level, position);
        boolean lowerHorizontalBranch = isBranch(below) && hasHorizontalBranch(level, position.below());
        boolean downConnected = isLeaves(below) || isGround(below) || connects(below)
            && (!horizontalBranch || isBranch(below) && !lowerHorizontalBranch);
        int mask = ConnectionRules.chooseMask(horizontal, isLeaves(above), isBranch(above),
            isWood(above) || isGround(above), downConnected, horizontalBranch);
        int variation = variants == 2
            ? (int) (position.asLong() ^ position.asLong() >>> 17) & 1 : 0;
        boolean linkBelow = (mask & ConnectionRules.DOWN) != 0 && lowerHorizontalBranch;
        return mask | variation << 6 | (linkBelow ? 128 : 0);
    }
}
