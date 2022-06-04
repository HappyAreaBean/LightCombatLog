package mc.obliviate.lightcombatlog;

import com.hakan.core.HCore;
import mc.obliviate.lightcombatlog.listeners.CombatListener;
import mc.obliviate.lightcombatlog.listeners.ReloadCMD;
import mc.obliviate.lightcombatlog.tag.CombatTagManager;
import mc.obliviate.lightcombatlog.tag.UntagTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LightCombatLog extends JavaPlugin {

	private final CombatTagManager combatTagManager = new CombatTagManager(this);
	private final UntagTask untagTask = new UntagTask(this);
	private YamlConfiguration configuration;

	@Override
	public void onEnable() {
		loadHandlers();
		loadListeners();
	}

	private void loadListeners() {
		Objects.requireNonNull(getCommand("lightcombatlog")).setExecutor(new ReloadCMD(this));
		HCore.initialize(this);

		final List<UUID> disabledWorlds = stringListToWorldUuidList(configuration.getStringList("disabled-worlds"));
		Bukkit.getPluginManager().registerEvents(new CombatListener(disabledWorlds, this, configuration.getInt("tag-time", 5000)), this);
	}

	private void loadHandlers() {
		loadConfig();
		untagTask.register(ChatColor.translateAlternateColorCodes('&', configuration.getString("timer.format", "ERROR")), configuration.getInt("combat-check-interval", 5));

	}

	private List<UUID> stringListToWorldUuidList(List<String> worldNames) {
		final List<UUID> worldUuids = new ArrayList<>();
		for (String worldName : worldNames) {
			World world = Bukkit.getWorld(worldName);
			if (world == null) {
				if (!worldName.equalsIgnoreCase("combatlogwilldisabledthisworld")) {
					getLogger().severe("The world " + worldName + " could not found.");
				}
				continue;
			}
			worldUuids.add(world.getUID());
		}
		return worldUuids;
	}

	public void loadConfig() {
		configuration = YamlConfiguration.loadConfiguration(new File(getDataFolder().getPath() + File.separator + "config.yml"));
		if (configuration.getKeys(false).isEmpty()) {
			saveResource("config.yml", true);
			configuration = YamlConfiguration.loadConfiguration(new File(getDataFolder().getPath() + File.separator + "config.yml"));
		}
	}

	public CombatTagManager getCombatTagManager() {
		return combatTagManager;
	}

	public UntagTask getUntagTask() {
		return untagTask;
	}

	public YamlConfiguration getConfiguration() {
		return configuration;
	}

	public String getConfigMessage(String key) {
		final String message = configuration.getString("messages." + key);
		if (message == null || message.isEmpty()) return null;
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public boolean isWorldGuardInstalled() {
		return Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
	}
}