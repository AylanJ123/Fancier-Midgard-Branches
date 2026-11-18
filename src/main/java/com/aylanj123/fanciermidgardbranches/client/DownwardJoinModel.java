package com.aylanj123.fanciermidgardbranches.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

final class DownwardJoinModel extends BakedModelWrapper<BakedModel> {
    private final List<BakedQuad> joined;

    DownwardJoinModel(BakedModel model, BakedModel link) {
        super(model);
        ArrayList<BakedQuad> quads = new ArrayList<>(
            model.getQuads(null, null, RandomSource.create(0)));
        quads.addAll(link.getQuads(null, null, RandomSource.create(0)));
        joined = List.copyOf(quads);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
        return side == null ? joined : originalModel.getQuads(state, side, random);
    }
}
