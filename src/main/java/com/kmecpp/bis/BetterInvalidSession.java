package com.kmecpp.bis;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.util.StringTranslate;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

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
	private static String OAuthKickMessage;

	private static boolean isReAuthLoaded;
	private static boolean isOAuthLoaded;

	private static final String DEFAULT_KICK_MESSAGE =
		"Invalid session (you probably opened another game launcher) \n\nRestart your game AND game launcher to login again";
	private static final String DEFAULT_REAUTH_KICK_MESSAGE =
		"Invalid session (you probably opened another game launcher) \n\nLogin again using the \"Re-Login\" button in the top left of the multiplayer menu or restart your game and game launcher";
	private static final String DEFAULT_OAUTH_KICK_MESSAGE =
			"Invalid session (you probably opened another game launcher) \n\nLogin again using the \"Login\" button in the top left of the multiplayer menu or restart your game and game launcher";

	private static boolean attempted = false;

	@Mod.EventHandler
	public void init(FMLLoadCompleteEvent event) {
		Configuration config = new Configuration(new File("config", "BetterInvalidSession.cfg"), VERSION);

		kickMessage = config.getString("kick-message", "general",
			DEFAULT_KICK_MESSAGE.replace("\n", "\\n"), "Kick message if the client is disconnected from a server due to having an invalid session");
		reAuthKickMessage = config.getString("reauth-kick-message", "general",
			DEFAULT_REAUTH_KICK_MESSAGE.replace("\n", "\\n"), "Kick message if ReAuth is installed");
		OAuthKickMessage = config.getString("oauth-kick-message", "general",
				DEFAULT_OAUTH_KICK_MESSAGE.replace("\n", "\\n"), "Kick message if OAuth is installed");

		isReAuthLoaded = Loader.isModLoaded("reauth");
		isOAuthLoaded = Loader.isModLoaded("oauth");

		kickMessage = kickMessage.replace("\\n", "\n");
		reAuthKickMessage = reAuthKickMessage.replace("\\n", "\n");
		OAuthKickMessage = OAuthKickMessage.replace("\\n", "\n");

		config.save();

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void on(GuiOpenEvent event) {
		if (!attempted && event.gui instanceof GuiMainMenu) {
			attempted = true;
			try {
				Field instanceField = StringTranslate.class.getDeclaredFields()[3]; //LanguageMap.class.getDeclaredField("instance");
				instanceField.setAccessible(true);
				StringTranslate t = (StringTranslate) instanceField.get(null);
				Field mapField = StringTranslate.class.getDeclaredFields()[2]; //LanguageMap.class.getDeclaredField("languageList");
				mapField.setAccessible(true);
				Map<String, String> m = (Map<String, String>) mapField.get(t);
				m.put("disconnect.loginFailedInfo.invalidSession", isReAuthLoaded ? reAuthKickMessage : isOAuthLoaded ? OAuthKickMessage : kickMessage);
				logger.info("Injected custom invalid session message successfully");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}


