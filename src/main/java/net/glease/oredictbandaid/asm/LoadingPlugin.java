package net.glease.oredictbandaid.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class LoadingPlugin implements IFMLLoadingPlugin {
    static boolean dev;
    static final Logger log = LogManager.getLogger("OredictTransformer");
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {"net.glease.oredictbandaid.asm.OredictTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        dev = !(boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
