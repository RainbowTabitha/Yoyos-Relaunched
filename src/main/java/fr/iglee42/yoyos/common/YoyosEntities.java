package fr.iglee42.yoyos.common;

import fr.iglee42.yoyos.Yoyos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

public class YoyosEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,Yoyos.MODID);

    public static RegistryObject<EntityType<YoyoEntity>> YOYO = ENTITY_TYPES.register("yoyo",()->EntityType.Builder.<YoyoEntity>of(YoyoEntity::new, MobCategory.MISC)
            .noSummon()
            .sized(0.25f, 0.25f)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .setShouldReceiveVelocityUpdates(true)
            .setCustomClientFactory((spawnEntity, level) ->new YoyoEntity(level))
            .build("yoyo"));

}
