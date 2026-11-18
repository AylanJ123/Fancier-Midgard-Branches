package com.aylanj123.fanciermidgardbranches.client;

import com.aylanj123.fanciermidgardbranches.FancierMidgardBranches;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = FancierMidgardBranches.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BranchModels {
    private static final String[] WOODS = {
        "acacia", "aspen", "birch", "cherry", "dark_oak", "jungle",
        "mangrove", "maple", "oak", "pine", "rough_aspen", "spruce"
    };

    private static ModelResourceLocation location(String variant, int mask) {
        return new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(
            FancierMidgardBranches.ID, "block/" + variant + "/branch_" + mask), "standalone");
    }

    private static ModelResourceLocation downLink(String variant) {
        return new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(
            FancierMidgardBranches.ID, "block/" + variant + "/down_link"), "standalone");
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        for (String wood : WOODS) {
            for (int variation = 0; variation < (wood.equals("aspen") ? 2 : 1); variation++) {
                String variant = wood.equals("aspen") ? wood + "_" + variation : wood;
                for (int mask = 0; mask < 64; mask++) {
                    event.register(location(variant, mask));
                }
                event.register(downLink(variant));
            }
        }
    }

    @SubscribeEvent
    public static void replaceModels(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BranchModelSet> models = new HashMap<>();
        for (String wood : WOODS) {
            int variants = wood.equals("aspen") ? 2 : 1;
            BakedModel[][] shapes = new BakedModel[variants][64];
            BakedModel[][] joined = new BakedModel[variants][64];
            for (int variation = 0; variation < variants; variation++) {
                String variant = wood.equals("aspen") ? wood + "_" + variation : wood;
                BakedModel link = event.getModels().get(downLink(variant));
                if (link == null) {
                    throw new IllegalStateException("Missing branch join " + downLink(variant));
                }
                for (int mask = 0; mask < 64; mask++) {
                    BakedModel model = event.getModels().get(location(variant, mask));
                    if (model == null) {
                        throw new IllegalStateException("Missing branch model " + location(variant, mask));
                    }
                    shapes[variation][mask] = model;
                    joined[variation][mask] = new DownwardJoinModel(model, link);
                }
            }
            models.put(ResourceLocation.fromNamespaceAndPath("midgard", wood + "_branch"),
                new BranchModelSet(shapes, joined));
        }
        event.getModels().replaceAll((id, original) -> {
            BranchModelSet shapes = models.get(id.id());
            return shapes != null && !id.variant().equals("inventory")
                ? new ConnectedBranchModel(original, shapes) : original;
        });
    }

}
