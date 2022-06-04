package mc.obliviate.lightcombatlog.tag;

import com.hakan.core.HCore;
import mc.obliviate.lightcombatlog.LightCombatLog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class UntagTask implements Runnable {

	private final LightCombatLog plugin;
	private String timerFormat;

	public UntagTask(final LightCombatLog plugin) {
		this.plugin = plugin;
	}

	public void register(final String timerFormat, final int checkInterval) {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 5L, checkInterval);
		this.timerFormat = timerFormat;
	}

	@Override
	public void run() {
		for (final Player player : new ArrayList<>(plugin.getCombatTagManager().tagMap.keySet())) {
			final long timeLeftMillis = plugin.getCombatTagManager().tagMap.get(player).getExpireTime() - System.currentTimeMillis();
			if (timeLeftMillis > 0) {
				//+1 second to get ceil() of time.
				showRemainingTime(player , timeLeftMillis + 1000);
				continue;
			}
			plugin.getCombatTagManager().untag(player);
		}
	}

	private void showRemainingTime(final Player player, final long timeLeftMillis) {
		HCore.sendActionBar(player, timerFormat.replace("{time}",((int) timeLeftMillis / 1000) + ""));
	}
}