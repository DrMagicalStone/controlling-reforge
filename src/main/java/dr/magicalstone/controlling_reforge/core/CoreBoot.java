package dr.magicalstone.controlling_reforge.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The entry class of the core mod.
 * The function of this class is to tell Mixin loader to load the Mixin config file "cotrolling_reforge.mixins.json".
 * So the most methods actually do nothing and the only important method is {@link CoreBoot#getMixinConfigs()} whose return refers to the Mixin config file.
 */
@IFMLLoadingPlugin.Name(ModInfo.NAME)
@IFMLLoadingPlugin.SortingIndex()
public class CoreBoot implements IFMLLoadingPlugin, IEarlyMixinLoader {
    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    /**
     *
     * @return the reference to Mixin config file src/main/resources/cotrolling_reforge.mixins.json.
     */
    public List<String> getMixinConfigs() {
        return Collections.singletonList("cotrolling_reforge.mixins.json");
    }
}
