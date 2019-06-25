package com.kmecpp.bis;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Logger;

@Mod(modid = BetterInvalidSession.NAME, name = BetterInvalidSession.NAME, version = BetterInvalidSession.VERSION)
public class BetterInvalidSession {

	public static final String NAME = "${name}";
	public static final String VERSION = "${version}";

	private static final Logger logger = Logger.getLogger(NAME);

	private static String kickMessage;
	private static String reAuthKickMessage;
	private static boolean isReAuthLoaded;

	private static final String DEFAULT_KICK_MESSAGE =
		"Invalid session (you logged in to MC somewhere else) \n\nRestart your game AND game launcher to login again";
	private static final String DEFAULT_REAUTH_KICK_MESSAGE =
		"Invalid session (you logged in to MC somewhere else) \n\nLogin again using the \"Re-Login\" button in the top left of the multiplayer menu or restart your game and game launcher";

	private static boolean attempted = false;

	@Mod.EventHandler
	public void init(FMLLoadCompleteEvent event) {
		Configuration config = new Configuration(new File("config", "BetterInvalidSession.cfg"), VERSION);
		kickMessage = config.getString("kick-message", "general",
			DEFAULT_KICK_MESSAGE.replace("\n", "\\n"), "Kick message if the client is disconnected from a server due to having an invalid session");
		reAuthKickMessage = config.getString("reauth-kick-message", "general",
			DEFAULT_REAUTH_KICK_MESSAGE.replace("\n", "\\n"), "Kick message if ReAuth is installed");
		isReAuthLoaded = Loader.isModLoaded("reauth");

		kickMessage = kickMessage.replace("\\n", "\n");
		reAuthKickMessage = reAuthKickMessage.replace("\\n", "\n");
		config.save();

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void on(GuiOpenEvent event) {
		if (!attempted && event.getGui() instanceof GuiMainMenu) {
			attempted = true;
			try {
				Field instanceField = ObfuscationReflectionHelper.findField(LanguageMap.class, "instance"); //LanguageMap.class.getDeclaredField("instance");
				instanceField.setAccessible(true);
				LanguageMap t = (LanguageMap) instanceField.get(null);
				Field mapField = ObfuscationReflectionHelper.findField(LanguageMap.class, "languageList"); //LanguageMap.class.getDeclaredField("languageList");
				mapField.setAccessible(true);
				Map<String, String> m = (Map<String, String>) mapField.get(t);
				m.put("disconnect.loginFailedInfo.invalidSession", isReAuthLoaded ? reAuthKickMessage : kickMessage);
				logger.info("Injected custom invalid session message successfully");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}


