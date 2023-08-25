package de.timecoding.cc.util;

import de.timecoding.cc.CubicCountdown;
import de.timecoding.cc.util.type.CubicStateType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;

public class CountdownModule {

    private CubicCountdown plugin;
    private CubicSettings cubicSettings;

    private int countdownId = -1;
    private int seconds = -1;

    public CountdownModule(CubicSettings settings) {
        this.cubicSettings = settings;
        this.plugin = this.cubicSettings.getPlugin();
    }

    public void start() {
        if (!isRunning()) {
            boolean next = true;
            for (CountdownModule countdownModule : plugin.getCountdownList()) {
                if (getCubicSettings().getCube() != null && countdownModule.getCubicSettings().getCube() != null && countdownModule.getCubicSettings().getCube().getName().equalsIgnoreCase(getCubicSettings().getCube().getName())) {
                    next = false;
                }
            }
            if (next) {
                this.seconds = this.cubicSettings.getCountdownSeconds();
                plugin.getCountdownList().add(this);
                if (cubicSettings.getStartDelay() >= 20 && cubicSettings.hasTitle(CubicStateType.START)) {
                    sendTitle(CubicStateType.START);
                }
                this.countdownId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        sendTitle(cubicSettings.getTitle(CubicStateType.PROCEED).replace("%seconds%", String.valueOf(seconds)), cubicSettings.getSubtitle(CubicStateType.PROCEED));
                        playSound(CubicStateType.PROCEED);
                        if (seconds <= 0) {
                            detonateFirework();
                            sendTitle(CubicStateType.END);
                            cubicSettings.fireworkLocations().forEach(location -> {
                                Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
                                firework.setFireworkMeta(cubicSettings.getFireworkMeta());
                                firework.detonate();
                            });
                            clearCube();
                            stop();
                        }
                        seconds--;
                    }
                }, cubicSettings.getStartDelay(), 20);
            }
        }
    }

    private void sendTitle(CubicStateType cubicStateType) {
        cubicSettings.playerList().forEach(player -> {
            if (cubicSettings.hasTitle(cubicStateType)) {
                player.sendTitle(cubicSettings.getTitle(cubicStateType), cubicSettings.getSubtitle(cubicStateType));
            }
        });
        //TRIGGERS AT THE SAME TIME
        this.playSound(cubicStateType);
    }

    private void playSound(CubicStateType cubicStateType) {
        cubicSettings.playerList().forEach(player -> {
            if (cubicSettings.hasSound(cubicStateType)) {
                player.playSound(player.getLocation(), cubicSettings.getSound(cubicStateType), 2, 2);
            }
        });
    }

    public void clearCube() {
        if (plugin.getConfigHandler().getBoolean("ClearCube.Enabled") && getCubicSettings().getCube() != null) {
            getCubicSettings().getCube().blockList(false).forEach(block -> {
                List<String> stringList = plugin.getConfigHandler().getStringList("ClearCube.DisabledBlocks");
                if (!stringList.contains(block.getType().toString())) {
                    block.setType(Material.AIR);
                }
            });
        }
    }

    private void sendTitle(String title, String subtitle) {
        cubicSettings.playerList().forEach(player -> player.sendTitle(title, subtitle));
    }

    public void cancel() {
        if (isRunning()) {
            sendTitle(CubicStateType.CANCELLED);
            this.stop();
        }
    }

    public void stop() {
        if (isRunning()) {
            Bukkit.getScheduler().cancelTask(this.countdownId);
            this.countdownId = -1;
            plugin.getCountdownList().remove(this);
        }
    }

    public void detonateFirework() {
        if (plugin.getConfigHandler().getBoolean("Firework.Enabled") && getCubicSettings().playerList().size() > 0) {
            Firework firework = (Firework) getCubicSettings().playerList().get(0).getWorld().spawnEntity(getCubicSettings().playerList().get(0).getLocation(), EntityType.FIREWORK);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            fireworkMeta.setPower(plugin.getConfigHandler().getInteger("Firework.Power"));
            this.getFireworkEffects().forEach(fireworkEffect -> {
                fireworkMeta.addEffect(fireworkEffect);
            });
            firework.setFireworkMeta(fireworkMeta);
            firework.detonate();
        }
    }

    private List<FireworkEffect> getFireworkEffects() {
        List<Color> list = new ArrayList<>();
        List<String> stringList = plugin.getConfigHandler().getStringList("Firework.Colors");
        List<FireworkEffect> fireworkEffects = new ArrayList<>();
        if (stringList != null) {
            stringList.forEach(s -> {
                fireworkEffects.add(FireworkEffect.builder().withColor(getColor(s)).build());
            });
        }
        return fireworkEffects;
    }

    private Color getColor(String s) {
        List<Color> list = new ArrayList<>();
        switch (s) {
            case "RED":
                list.add(Color.RED);
                break;
            case "AQUA":
                list.add(Color.AQUA);
                break;
            case "BLUE":
                list.add(Color.BLUE);
                break;
            case "LIME":
                list.add(Color.LIME);
                break;
            case "OLIVE":
                list.add(Color.OLIVE);
                break;
            case "ORANGE":
                list.add(Color.ORANGE);
                break;
            case "PURPLE":
                list.add(Color.PURPLE);
                break;
            case "WHITE":
                list.add(Color.WHITE);
                break;
            case "BLACK":
                list.add(Color.BLACK);
                break;
            case "FUCHSIA":
                list.add(Color.FUCHSIA);
                break;
            case "GREY":
                list.add(Color.GRAY);
                break;
            case "GREEN":
                list.add(Color.GREEN);
                break;
            case "MAROON":
                list.add(Color.MAROON);
                break;
            case "NAVY":
                list.add(Color.NAVY);
                break;
            case "SILVER":
                list.add(Color.SILVER);
                break;
            case "YELLOW":
                list.add(Color.YELLOW);
                break;
            case "TEAL":
                list.add(Color.TEAL);
                break;
        }
        if (list.size() == 0) {
            return Color.GRAY;
        } else {
            return list.get(0);
        }
    }

    public boolean isRunning() {
        return (seconds != -1);
    }

    public int getSecondsLeft() {
        return seconds;
    }

    public int getCountdownId() {
        return countdownId;
    }

    public CubicSettings getCubicSettings() {
        return cubicSettings;
    }

    public CubicCountdown getPlugin() {
        return plugin;
    }
}