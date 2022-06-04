package mc.obliviate.lightcombatlog.listeners;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import mc.obliviate.lightcombatlog.LightCombatLog;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.UUID;

public class CombatListener implements Listener {

	private final List<UUID> disabledWorldUuids;
	private final LightCombatLog plugin;
	private final int tagTime;

	public CombatListener(List<UUID> disabledWorldUuids, LightCombatLog plugin, int tagTime) {
		this.disabledWorldUuids = disabledWorldUuids;
		this.plugin = plugin;
		this.tagTime = tagTime;
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (disabledWorldUuids.contains(e.getDamager().getWorld().getUID())) return;
		if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
			if (e.getEntity().hasMetadata("NPC")) return;
			if (plugin.isWorldGuardInstalled()) {
				Player player = ((Player) e.getDamager());
				LocalPlayer localPlayer = WGBukkit.getPlugin().wrapPlayer(player);
				RegionQuery query = WGBukkit.getPlugin().getRegionContainer().createQuery();
				StateFlag.State state = query.queryState(player.getLocation(), localPlayer, DefaultFlag.PVP);
				if (state == StateFlag.State.DENY) {
					return;
				}
			}
			plugin.getCombatTagManager().tag((Player) e.getDamager(), (Player) e.getEntity(), tagTime);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (plugin.getConfiguration().getStringList("executable-commands-during-combat").contains(e.getMessage()))
			return;
		if (plugin.getCombatTagManager().isTagged(e.getPlayer())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(plugin.getConfigMessage("you-can-not-execute-this-command"));
		}
	}

//	@EventHandler(ignoreCancelled = true)
//	public void onInventoryOpen(InventoryOpenEvent e) {
//		final Player player = (Player) e.getPlayer();
//		if (plugin.getCombatTagManager().isTagged(player)) {
//			e.setCancelled(true);
//			player.sendMessage(plugin.getConfigMessage("you-can-not-open-inventory"));
//		}
//	}

//	@EventHandler(ignoreCancelled = true)
//	public void onInventoryOpen(PlayerTeleportEvent e) {
//		if (plugin.getCombatTagManager().isTagged(e.getPlayer())) {
//			e.setCancelled(true);
//			e.getPlayer().sendMessage(plugin.getConfigMessage("you-can-not-teleport"));
//		}
//	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if (plugin.getCombatTagManager().isTagged(e.getEntity())) {
			plugin.getCombatTagManager().untag(e.getEntity());
		}
	}

	@EventHandler
	public void onDisconnect(PlayerQuitEvent e) {
		if (plugin.getCombatTagManager().isTagged(e.getPlayer())) {
			e.getPlayer().setHealth(0d);
			//player will be untagged in onDeathEvent
		}
	}
}