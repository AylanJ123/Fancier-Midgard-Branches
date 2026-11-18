package com.aylanj123.fanciermidgardbranches.client;

import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

final class ConnectedBranchModel extends BakedModelWrapper<BakedModel>
        implements IDynamicBakedModel {
    private static final ModelProperty<Integer> CONNECTION_MASK = new ModelProperty<>();
    private final BranchModelSet shapes;

    ConnectedBranchModel(BakedModel original, BranchModelSet shapes) {
        super(original);
        this.shapes = shapes;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos position, BlockState state,
                                  ModelData existing) {
        int connections = BranchConnections.resolve(level, position, state, shapes.normal().length);
        return existing.derive().with(CONNECTION_MASK, connections).build();
    }

    private BakedModel shape(ModelData data) {
        Integer encoded = data.get(CONNECTION_MASK);
        int key = encoded == null ? ConnectionRules.UP | ConnectionRules.DOWN : encoded;
        int variation = (key >>> 6 & 1) % shapes.normal().length;
        return ((key & 128) != 0 ? shapes.joined() : shapes.normal())[variation][key & 63];
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random,
                                   ModelData data, RenderType renderType) {
        return shape(data).getQuads(state, side, random);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
        return shapes.normal()[0][ConnectionRules.UP | ConnectionRules.DOWN].getQuads(state, side, random);
    }
}
