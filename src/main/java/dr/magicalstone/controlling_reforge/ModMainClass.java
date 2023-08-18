package dr.magicalstone.controlling_reforge;

import dr.magicalstone.controlling_reforge.core.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.apache.logging.log4j.Logger;

/**
 * The main class of this mod (Controlling Reforge) who tells FML modid, name and other information of this mod.
 * The main class also contains a logger for this mod.
 */
@Mod(modid = ModInfo.MOD_ID, name = ModInfo.NAME, clientSideOnly = true, useMetadata = true)
public class ModMainClass {

    private static Logger logger;

    @Mod.EventHandler
    public void initLogger(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    public static Logger getLogger() {
        return logger;
    }

}
