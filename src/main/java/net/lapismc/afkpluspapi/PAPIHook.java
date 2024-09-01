package net.lapismc.afkpluspapi;

import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.api.AFKPlusPlayerAPI;
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import net.lapismc.lapiscore.placeholder.PlaceholderAPIExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.ocpsoft.prettytime.Duration;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

public class PAPIHook extends PlaceholderAPIExpansion {

    private final AFKPlusPlayerAPI api;
    private final AFKPlusPAPI plugin;

    public PAPIHook(AFKPlusPAPI plugin) {
        super(AFKPlus.getInstance());
        this.plugin = plugin;
        api = new AFKPlusPlayerAPI();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if ("Status".equalsIgnoreCase(identifier)) {
            return api.getPlayer(player).isAFK() ? plugin.getConfig().getString("status.true")
                    : plugin.getConfig().getString("status.false");
        } else if ("AFKTime".equalsIgnoreCase(identifier)) {
            AFKPlusPlayer p = api.getPlayer(player);
            Long afkStart = p.getAFKStart();
            if (afkStart == null) {
                return plugin.getConfig().getString("afktime.notafk");
            }
            //Get the list of durations for this time difference, reduce that list to the configured amount
            List<Duration> totalTimeDurations = reduceDurationList(new PrettyTime(
                    new Date(0)).calculatePreciseDuration(new Date(afkStart)));
            //Get pretty time to format the remaining durations without future or past context
            return new PrettyTime().formatDuration(totalTimeDurations);
        } else if ("TotalTimeAFK".equalsIgnoreCase(identifier)) {
            AFKPlusPlayer p = api.getPlayer(player);
            //Get the total time in milliseconds
            long totalTime = p.getTotalTimeAFK();
            //Get the list of durations for this time difference, reduce that list to the configured amount
            List<Duration> totalTimeDurations = reduceDurationList(new PrettyTime(
                    new Date(0)).calculatePreciseDuration(new Date(totalTime)));
            //Get pretty time to format the remaining durations without future or past context
            return new PrettyTime().formatDuration(totalTimeDurations);
        }
        return null;
    }

    private List<Duration> reduceDurationList(List<Duration> durationList) {
        while (durationList.size() > plugin.getConfig().getInt("TotalTimeAFK.numberOfTimeUnits")) {
            Duration smallest = null;
            for (Duration current : durationList) {
                if (smallest == null || smallest.getUnit().getMillisPerUnit() > current.getUnit().getMillisPerUnit()) {
                    smallest = current;
                }
            }
            durationList.remove(smallest);
        }
        return durationList;
    }
}
