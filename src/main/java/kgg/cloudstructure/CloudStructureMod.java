package kgg.cloudstructure;


import kgg.cloudstructure.config.CloudStructureConfig;
import kgg.cloudstructure.network.CloudStructureNetwork;
import net.minecraftforge.fml.common.Mod;

@Mod(value = CloudStructureMod.MOD_ID)
@Mod.EventBusSubscriber
public class CloudStructureMod {
    public static final String MOD_ID = "cloudstructure";

    public CloudStructureMod() {
        CloudStructureConfig.register();
        CloudStructureNetwork.register();
    }
}
