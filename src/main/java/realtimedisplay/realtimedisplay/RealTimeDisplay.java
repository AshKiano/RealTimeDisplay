package realtimedisplay.realtimedisplay;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class RealTimeDisplay extends JavaPlugin implements CommandExecutor {
    private HashMap<UUID, Boolean> playerTimeDisplay = new HashMap<>();
    private File playerDataFile;
    private FileConfiguration playerDataConfig;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    public void onEnable() {
        this.getCommand("realtime").setExecutor(this);
        createPlayerDataFile();
        loadPlayerSettings();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (playerTimeDisplay.getOrDefault(player.getUniqueId(), false)) {
                        String time = dateFormat.format(new Date());
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(time));
                    }
                }
            }
        }.runTaskTimer(this, 0, 20); // Updates every second

        Metrics metrics = new Metrics(this, 21910);
        this.getLogger().info("Thank you for using the RealTimeDisplay plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://donate.ashkiano.com");
    }

    @Override
    public void onDisable() {
        savePlayerSettings();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            boolean currentState = playerTimeDisplay.getOrDefault(player.getUniqueId(), false);
            playerTimeDisplay.put(player.getUniqueId(), !currentState);
            savePlayerSettings();
            if (!currentState) {
                player.sendMessage(ChatColor.GREEN + "Real-time display enabled.");
            } else {
                player.sendMessage(ChatColor.RED + "Real-time display disabled.");
            }
        }
        return true;
    }

    private void createPlayerDataFile() {
        playerDataFile = new File(getDataFolder(), "playerData.yml");
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            saveResource("playerData.yml", false);
        }
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    private void savePlayerSettings() {
        for (UUID playerId : playerTimeDisplay.keySet()) {
            playerDataConfig.set(playerId.toString(), playerTimeDisplay.get(playerId));
        }
        try {
            playerDataConfig.save(playerDataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPlayerSettings() {
        if (playerDataConfig == null) return;
        for (String key : playerDataConfig.getKeys(false)) {
            playerTimeDisplay.put(UUID.fromString(key), playerDataConfig.getBoolean(key));
        }
    }
}