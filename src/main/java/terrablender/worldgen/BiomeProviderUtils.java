/*******************************************************************************
 * Copyright 2022, the Glitchfiend Team.
 * Creative Commons Attribution-NonCommercial-NoDerivatives 4.0.
 ******************************************************************************/
package terrablender.worldgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.api.BiomeProvider;
import terrablender.api.BiomeProviders;
import terrablender.api.GenerationSettings;
import terrablender.worldgen.surface.NamespacedSurfaceRuleSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class BiomeProviderUtils
{
    public static SurfaceRules.RuleSource createOverworldRules(SurfaceRules.RuleSource base)
    {
        return createNamespacedRuleSource(base, provider -> provider.getOverworldSurfaceRules());
    }

    public static SurfaceRules.RuleSource createOverworldRules()
    {
        return createOverworldRules(GenerationSettings.getDefaultOverworldSurfaceRules());
    }

    public static SurfaceRules.RuleSource createNetherRules(SurfaceRules.RuleSource base)
    {
        return createNamespacedRuleSource(base, provider -> provider.getNetherSurfaceRules());
    }

    public static SurfaceRules.RuleSource createNetherRules()
    {
        return createNetherRules(GenerationSettings.getDefaultNetherSurfaceRules());
    }

    private static SurfaceRules.RuleSource createNamespacedRuleSource(SurfaceRules.RuleSource base, Function<BiomeProvider, Optional<SurfaceRules.RuleSource>> source)
    {
        return new NamespacedSurfaceRuleSource(base, ImmutableMap.copyOf(collectRuleSources(source)));
    }

    public static float getUniquenessRangeSize()
    {
        return 2.0F / (float)BiomeProviders.getCount();
    }

    public static float getUniquenessMidPoint(int index)
    {
        float rangeSize = getUniquenessRangeSize();
        return -1.0F + rangeSize * index + (rangeSize * 0.5F);
    }

    public static Climate.Parameter getUniquenessParameter(int index)
    {
        float min = -1.0F + getUniquenessRangeSize() * index;
        float max = -1.0F + getUniquenessRangeSize() * (index + 1);
        return Climate.Parameter.span(min, max);
    }

    public static TBClimate.ParameterPoint convertParameterPoint(Climate.ParameterPoint point, Climate.Parameter uniqueness)
    {
        return TBClimate.parameters(point.temperature(), point.humidity(), point.continentalness(), point.erosion(), point.depth(), point.weirdness(), uniqueness, Climate.unquantizeCoord(point.offset()));
    }

    public static List<TBClimate.ParameterPoint> getAllSpawnTargets()
    {
        ImmutableList.Builder<TBClimate.ParameterPoint> builder = new ImmutableList.Builder<>();
        BiomeProviders.get().forEach(provider -> builder.addAll(provider.getSpawnTargets()));
        return builder.build();
    }

    public static void addAllBiomes(Registry<Biome> registry, Consumer<Pair<TBClimate.ParameterPoint, ResourceKey<Biome>>> mapper)
    {
        BiomeProviders.get().forEach(provider -> provider.addOverworldBiomes(registry, mapper));
    }

    private static Map<String, SurfaceRules.RuleSource> collectRuleSources(Function<BiomeProvider, Optional<SurfaceRules.RuleSource>> rulesSource)
    {
        ImmutableMap.Builder<String, SurfaceRules.RuleSource> builder = new ImmutableMap.Builder();

        for (BiomeProvider provider : BiomeProviders.get())
        {
            Optional<SurfaceRules.RuleSource> rules = provider.getOverworldSurfaceRules();
            if (rules.isPresent()) builder.put(provider.getName().getNamespace(), rules.get());
        }

        return builder.build();
    }
}