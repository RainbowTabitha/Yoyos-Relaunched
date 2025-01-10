package fr.iglee42.yoyos.network;

import fr.iglee42.yoyos.Yoyos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class YoyosNetwork {

    public static final String PROTOCOL = "1.0";
    public static SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(Yoyos.MODID,"messages"),
            ()->PROTOCOL,
            s->true,
            s->true);

    private static int ID = 0;
    private static int id(){
        return ID++;
    }

    public static void init(){
        CHANNEL.registerMessage(id(), CollectedDropsSyncS2C.class,CollectedDropsSyncS2C::encode,CollectedDropsSyncS2C::new,CollectedDropsSyncS2C::handle);
    }
}
