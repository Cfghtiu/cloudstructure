package kgg.cloudstructure.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class CloudStructureConfig {
    private final ForgeConfigSpec.ConfigValue<String> url;
    private final ForgeConfigSpec.ConfigValue<String> user;
    private final ForgeConfigSpec.ConfigValue<String> passwd;

    private CloudStructureConfig(ForgeConfigSpec.Builder builder) {
        url = builder.comment("上传和下载的网址").define("url", "");
        user = builder.comment("用户名").define("user", "");
        passwd = builder.comment("密码(不要泄露)").define("passwd", "");
    }

    public String getUrl() {
        return url.get();
    }

    public String getUser() {
        return user.get();
    }

    public String getPasswd() {
        return passwd.get();
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public void setUser(String user) {
        this.user.set(user);
    }

    public void setPasswd(String passwd) {
        this.passwd.set(passwd);
    }

    public boolean isEmpty() {
        return url.get().isEmpty() || user.get().isEmpty();
    }


    public static ForgeConfigSpec SPEC;
    public static CloudStructureConfig CONFIG;
    public static void register() {
        Pair<CloudStructureConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(CloudStructureConfig::new);
        SPEC = pair.getValue();
        CONFIG = pair.getKey();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC);
    }
}
