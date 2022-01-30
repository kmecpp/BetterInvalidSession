package com.kmecpp.bis;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.resources.ClientLanguageMap;
import net.minecraft.util.text.LanguageMap;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

@Mod(BetterInvalidSession.ID)
public class BetterInvalidSession {

	public static final String ID = "betterinvalidsession";

	private static final Logger logger = LogManager.getLogger();

	private static String kickMessage;
	private static String reAuthKickMessage;
	private static boolean isReAuthLoaded;

	private static final String DEFAULT_KICK_MESSAGE =
		"Invalid session (you probably opened another game launcher) \n\nRestart your game AND game launcher to login again";
	private static final String DEFAULT_REAUTH_KICK_MESSAGE =
		"Invalid session (you probably opened another game launcher) \n\nLogin again using the \"Re-Login\" button in the top left of the multiplayer menu or restart your game and game launcher";

	private static boolean attempted = false;

	public BetterInvalidSession() {
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		MinecraftForge.EVENT_BUS.register(this);

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);

		CommentedFileConfig config = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve("BetterInvalidSession.toml"))
			.writingMode(WritingMode.REPLACE)
			.build();

		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		ForgeConfigSpec.ConfigValue<String> KICK_MESSAGE_CONFIG = builder
			.comment("Kick message if the client is disconnected from a server due to having an invalid session")
			.define("kick-message", DEFAULT_KICK_MESSAGE.replace("\n", "\\n"));

		ForgeConfigSpec.ConfigValue<String> REAUTH_KICK_MESSAGE_CONFIG = builder
			.comment("Kick message if ReAuth is installed")
			.define("reauth-kick-message", DEFAULT_REAUTH_KICK_MESSAGE.replace("\n", "\\n"));

		ForgeConfigSpec spec = builder.build();
		spec.setConfig(config);

		kickMessage = KICK_MESSAGE_CONFIG.get().replace("\\n", "\n");
		reAuthKickMessage = REAUTH_KICK_MESSAGE_CONFIG.get().replace("\\n", "\n");

		config.save();
	}

	private void onLoadComplete(FMLLoadCompleteEvent event) {
		isReAuthLoaded = ModList.get().isLoaded("reauth");
	}

	@SubscribeEvent
	public void on(GuiOpenEvent event) {
		if (!attempted && event.getGui() instanceof MainMenuScreen) {
			attempted = true;
			try {
				long start = System.nanoTime();
				LanguageMap languageMap = LanguageMap.getInstance();
				Field mapField = ClientLanguageMap.class.getDeclaredFields()[1]; //LanguageMap.class.getDeclaredField("languageList");
				mapField.setAccessible(true);

				@SuppressWarnings("unchecked")
				LinkedHashMap<String, String> mapCopy = new LinkedHashMap<>((ImmutableMap<String, String>) mapField.get(languageMap));
				mapCopy.put("disconnect.loginFailedInfo.invalidSession", isReAuthLoaded ? reAuthKickMessage : kickMessage);
				mapField.set(languageMap, ImmutableMap.copyOf(mapCopy));

				long end = System.nanoTime();
				logger.info("Injected custom invalid session message successfully (" + (int) ((end - start) / 1000F) + "us)");

//				Field entriesField = m.getClass().getDeclaredFields()[0];
//				Field tableField = m.getClass().getDeclaredFields()[1];
//
//				tableField.setAccessible(true);
//				entriesField.setAccessible(true);
//
//				Field modifiersField = Field.class.getDeclaredField("modifiers");
//				modifiersField.setAccessible(true);
//				modifiersField.setInt(tableField, tableField.getModifiers() & ~Modifier.FINAL);
//				modifiersField.setInt(entriesField, entriesField.getModifiers() & ~Modifier.FINAL);
//
//				Object[] table = (Object[]) tableField.get(m);
//				Object[] entries = (Object[]) entriesField.get(m);
//
//				System.out.println(entries[3887].getClass());
//				System.out.println(table[3887].getClass());
//
//				Class<?> immutableMapEntryClass = table[3887].getClass();
//				Class<?> entryClass = Class.forName("");
//
//				Field entryKeyField = immutableMapEntryClass.getField("key");
//				Field entryValueField = immutableMapEntryClass.getField("value");
//				entryKeyField.setAccessible(true);
//				entryValueField.setAccessible(true);
//
//				System.out.println(immutableMapEntryClass);
//				System.out.println(entryKeyField.get(table[3887]));
//				System.out.println(entryValueField.get(table[3887]));
//
////				entries[3887] =
//
////				for (int i = 3800; i < 4000; i++) {
////
////				}
////				int i = 0;
////				for (Map.Entry<String, String> e : m.entrySet()) {
////					System.out.println(i + " )" + e.getKey() + " : " + e.getValue());
////					i++;
////				}
//				System.out.println(m.getClass());
////				3887
//				m.put("disconnect.loginFailedInfo.invalidSession", isReAuthLoaded ? reAuthKickMessage : kickMessage);
//
//				modifiersField.setInt(tableField, tableField.getModifiers() & Modifier.FINAL);
//				modifiersField.setInt(entriesField, entriesField.getModifiers() & Modifier.FINAL);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}


