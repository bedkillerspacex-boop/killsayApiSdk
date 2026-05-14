package mojang.minecraft.uuidget;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class ClientInitializer implements ClientModInitializer {
    private static final Map<UUID, PendingVictim> pending = new HashMap<>();
    private static final Map<String, Long> chatWatch = new HashMap<>();
    private static final Map<Integer, Set<String>> trackedProj = new HashMap<>();
    private static final Pattern DAMAGE_MSG = Pattern.compile("玩家\\s+(\\S+?)\\s+受到");
    private static final long DEATH_VOTE_WINDOW_MS = 4000L;

    private static long doneUntil = 0L;
    private static long deathUntil = 0L;
    private static long victoryUntil = 0L;
    private static long recentKillUntil = 0L;
    private static String doneName = "";
    private static net.minecraft.world.World lastWorld = null;
    private static net.minecraft.registry.RegistryKey<net.minecraft.world.World> lastDimension = null;
    private static double lastX = Double.NaN;
    private static double lastY = Double.NaN;
    private static double lastZ = Double.NaN;
    private static net.minecraft.world.GameMode lastGameMode = null;
    private static boolean lastAllowFlying = false;
    private static boolean lastPlayerInvisible = false;
    private static long deathVoteTp = 0L;
    private static long deathVoteTitle = 0L;
    private static long deathVoteSpectator = 0L;
    private static long deathVoteSelfName = 0L;

    private record PendingVictim(String name, long deadline) {}

    @Override
    public void onInitializeClient() {
        ClientOptions.load();
        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (player == mc.player && target instanceof PlayerEntity victim && victim != mc.player) {
                mark(victim);
            }
            return ActionResult.PASS;
        });
    }

    public static boolean isDeadPause() {
        return System.currentTimeMillis() < deathUntil;
    }

    public static boolean isVictory() {
        return System.currentTimeMillis() < victoryUntil;
    }

    public static String getDoneName() {
        return doneName;
    }

    public static boolean isTracking(UUID uuid) {
        return pending.containsKey(uuid);
    }

    public static void onChatMessage(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        String clean = text.replaceAll("§.", "");
        var matcher = DAMAGE_MSG.matcher(clean);
        if (matcher.find()) {
            String name = matcher.group(1);
            MinecraftClient mc = MinecraftClient.getInstance();
            PlayerEntity player = findPlayerByName(mc, name);
            if (player != null) {
                mark(player);
            } else {
                chatMark(name);
            }
        }
    }

    public static void onGameMessage(Text message, boolean overlay) {
        if (overlay || message == null) {
            return;
        }
        String text = message.getString().replaceAll("§.", "");
        long now = System.currentTimeMillis();
        MinecraftClient mc = MinecraftClient.getInstance();

        if (text.contains("胜利")) {
            victoryUntil = now + 10_000L;
            KillsayEvents.fireOnVictory();
            return;
        }

        if (mc.player != null) {
            String selfName = mc.player.getGameProfile().getName();
            if (selfName != null && !selfName.isEmpty() && text.contains(selfName)) {
                boolean isKillAnnouncement = false;
                for (var p : pending.values()) {
                    if (p.name() != null && text.contains(p.name())) {
                        isKillAnnouncement = true;
                        break;
                    }
                }
                if (!isKillAnnouncement) {
                    for (String watchName : chatWatch.keySet()) {
                        if (text.contains(watchName)) {
                            isKillAnnouncement = true;
                            break;
                        }
                    }
                }
                if (!isKillAnnouncement) {
                    castDeathVote("selfname");
                }
            }
        }

        if (!pending.isEmpty() && !isDeadPause()) {
            Iterator<Map.Entry<UUID, PendingVictim>> it = pending.entrySet().iterator();
            while (it.hasNext()) {
                var entry = it.next();
                PendingVictim victim = entry.getValue();
                if (now > victim.deadline()) {
                    continue;
                }
                if (victim.name() != null && text.contains(victim.name())) {
                    it.remove();
                    chatWatch.remove(victim.name());
                    recordDetection(victim.name());
                    break;
                }
            }
        }

        String matched = null;
        for (var entry : chatWatch.entrySet()) {
            if (now <= entry.getValue() && text.contains(entry.getKey())) {
                matched = entry.getKey();
                break;
            }
        }
        if (matched != null) {
            chatWatch.remove(matched);
            if (!isDeadPause()) {
                recordDetection(matched);
            }
        }
        chatWatch.entrySet().removeIf(e -> now > e.getValue());
    }

    public static void onTransfer() {
        resetOnTransfer();
    }

    public static void onTitleReceived(String text) {
        if (text == null) {
            return;
        }
        String clean = text.replaceAll("§.", "");
        if (clean.contains("胜利")) {
            victoryUntil = System.currentTimeMillis() + 10_000L;
            KillsayEvents.fireOnVictory();
            return;
        }
        castDeathVote("title");
    }

    public static void onClientTick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            lastWorld = null;
            lastDimension = null;
            lastX = Double.NaN;
            lastY = Double.NaN;
            lastZ = Double.NaN;
            lastGameMode = null;
            lastAllowFlying = false;
            lastPlayerInvisible = false;
            return;
        }

        if (lastWorld != mc.world || lastDimension != mc.world.getRegistryKey()) {
            if (lastWorld != null || lastDimension != null) {
                resetOnTransfer();
            }
            lastWorld = mc.world;
            lastDimension = mc.world.getRegistryKey();
            lastX = lastY = lastZ = Double.NaN;
        }

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();
        if (!Double.isNaN(lastX)) {
            double dx = px - lastX;
            double dy = py - lastY;
            double dz = pz - lastZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > 64.0 * 64.0) {
                resetOnTransfer();
            } else if (distSq > 10.0 * 10.0) {
                castDeathVote("tp");
            }
        }
        lastX = px;
        lastY = py;
        lastZ = pz;

        net.minecraft.world.GameMode curMode = mc.interactionManager != null
                ? mc.interactionManager.getCurrentGameMode()
                : null;
        boolean curAllowFlying = mc.player.getAbilities().allowFlying;
        if (curMode != lastGameMode) {
            if (lastGameMode != null && curMode == net.minecraft.world.GameMode.SPECTATOR) {
                castDeathVote("spectator");
            }
            lastGameMode = curMode;
        }
        if (curAllowFlying != lastAllowFlying) {
            if (curAllowFlying && curMode == net.minecraft.world.GameMode.SURVIVAL) {
                castDeathVote("spectator");
            }
            lastAllowFlying = curAllowFlying;
        }

        boolean curInvisible = mc.player.isInvisible();
        if (curInvisible && !lastPlayerInvisible) {
            castDeathVote("spectator");
        }
        lastPlayerInvisible = curInvisible;

        scanProjectiles(mc);

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, PendingVictim>> it = pending.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            PendingVictim victim = entry.getValue();
            if (now > victim.deadline()) {
                it.remove();
                continue;
            }

            PlayerEntity player = mc.world.getPlayerByUuid(entry.getKey());
            if (player == null) {
                if (!chatWatch.containsKey(victim.name())) {
                    chatWatch.put(victim.name(), victim.deadline());
                }
                it.remove();
                continue;
            }

            var reason = player.getRemovalReason();
            boolean hardDead = player.isDead() || player.getHealth() <= 0f
                    || (reason != null && reason.shouldDestroy());
            boolean softDead = !hardDead && player.isInvisible();

            if (hardDead) {
                it.remove();
                chatWatch.remove(victim.name());
                if (!isDeadPause()) {
                    recordDetection(victim.name());
                }
            } else if (softDead) {
                if (!chatWatch.containsKey(victim.name())) {
                    chatWatch.put(victim.name(), victim.deadline());
                }
                it.remove();
            }
        }
    }

    public static boolean isEnabled() {
        return ClientOptions.enabled();
    }

    static List<KillsayEvents.PlayerTrackingInfo> snapshotTrackedPlayers() {
        java.util.ArrayList<KillsayEvents.PlayerTrackingInfo> list = new java.util.ArrayList<>();
        for (var e : pending.entrySet()) {
            if (e.getValue().name() != null) {
                float health = HealthTracker.getPredictedHealth(e.getKey());
                list.add(new KillsayEvents.PlayerTrackingInfo(e.getValue().name(), e.getValue().deadline(), "attack", health));
            }
        }
        for (var e : chatWatch.entrySet()) {
            boolean exists = list.stream().anyMatch(t -> t.name().equals(e.getKey()));
            if (!exists) {
                list.add(new KillsayEvents.PlayerTrackingInfo(e.getKey(), e.getValue(), "chat", -1.0f));
            }
        }
        return List.copyOf(list);
    }

    static KillsayEvents.ConfigSnapshot getConfigSnapshot() {
        return new KillsayEvents.ConfigSnapshot(
                ClientOptions.enabled(),
                ClientOptions.chatProjectileDetect(),
                ClientOptions.transferGuard(),
                ClientOptions.windowSeconds()
        );
    }

    private static void mark(PlayerEntity victim) {
        String name = victim.getGameProfile().getName();
        if (name != null && name.startsWith("CIT-")) {
            return;
        }
        long deadline = System.currentTimeMillis() + (long) (ClientOptions.windowSeconds() * 1000.0);
        pending.put(victim.getUuid(), new PendingVictim(name, deadline));
    }

    private static void chatMark(String name) {
        if (name == null || name.startsWith("CIT-")) {
            return;
        }
        long deadline = System.currentTimeMillis() + (long) (ClientOptions.windowSeconds() * 1000.0);
        chatWatch.put(name, deadline);
    }

    private static void scanProjectiles(MinecraftClient mc) {
        if (ClientOptions.chatProjectileDetect()) {
            Set<Integer> aliveIds = new HashSet<>();
            for (Entity ent : mc.world.getEntities()) {
                if (!(ent instanceof ProjectileEntity proj) || proj.getOwner() != mc.player) {
                    continue;
                }
                int id = proj.getId();
                aliveIds.add(id);
                Box box = proj.getBoundingBox().expand(ClientOptions.trackingExpand());
                Set<String> nearby = trackedProj.computeIfAbsent(id, k -> new HashSet<>());
                for (PlayerEntity pp : mc.world.getPlayers()) {
                    if (pp == mc.player) {
                        continue;
                    }
                    String name = pp.getGameProfile().getName();
                    if (name != null && name.startsWith("CIT-")) {
                        continue;
                    }
                    if (pp.getBoundingBox().intersects(box)) {
                        nearby.add(name);
                    }
                }
            }
            Iterator<Map.Entry<Integer, Set<String>>> pit = trackedProj.entrySet().iterator();
            while (pit.hasNext()) {
                var entry = pit.next();
                if (!aliveIds.contains(entry.getKey())) {
                    for (String name : entry.getValue()) {
                        chatMark(name);
                    }
                    pit.remove();
                }
            }
            if (trackedProj.size() > 64) {
                trackedProj.clear();
            }
        } else {
            for (Entity ent : mc.world.getEntities()) {
                if (!(ent instanceof ProjectileEntity proj) || proj.getOwner() != mc.player) {
                    continue;
                }
                Box box = proj.getBoundingBox().expand(ClientOptions.simpleExpand());
                for (PlayerEntity pp : mc.world.getPlayers()) {
                    if (pp != mc.player && pp.getBoundingBox().intersects(box)) {
                        mark(pp);
                        break;
                    }
                }
            }
            trackedProj.clear();
        }
    }

    private static void resetOnTransfer() {
        if (!ClientOptions.transferGuard()) {
            return;
        }
        pending.clear();
        chatWatch.clear();
        trackedProj.clear();
        HealthTracker.clear();
        deathVoteTp = 0L;
        deathVoteTitle = 0L;
        deathVoteSpectator = 0L;
        deathVoteSelfName = 0L;
    }

    private static void triggerDeath() {
        long now = System.currentTimeMillis();
        pending.clear();
        chatWatch.clear();
        trackedProj.clear();
        HealthTracker.clear();
        deathUntil = now + 5000L;
        deathVoteTp = 0L;
        deathVoteTitle = 0L;
        deathVoteSpectator = 0L;
        deathVoteSelfName = 0L;
        KillsayEvents.fireOnDeath();
    }

    private static void castDeathVote(String signal) {
        long now = System.currentTimeMillis();
        switch (signal) {
            case "tp" -> deathVoteTp = now;
            case "title" -> deathVoteTitle = now;
            case "spectator" -> deathVoteSpectator = now;
            case "selfname" -> {
                if (now < recentKillUntil) {
                    return;
                }
                deathVoteSelfName = now;
            }
            default -> {
                return;
            }
        }

        int votes = 0;
        if (now - deathVoteTp <= DEATH_VOTE_WINDOW_MS) votes++;
        if (now - deathVoteTitle <= DEATH_VOTE_WINDOW_MS) votes++;
        if (now - deathVoteSpectator <= DEATH_VOTE_WINDOW_MS) votes++;
        if (now - deathVoteSelfName <= DEATH_VOTE_WINDOW_MS) votes++;
        if (votes >= 2) {
            triggerDeath();
        }
    }

    private static void recordDetection(String victimName) {
        doneName = victimName == null ? "" : victimName;
        doneUntil = System.currentTimeMillis() + 3000L;
        recentKillUntil = System.currentTimeMillis() + 500L;
        KillsayEvents.fireUserKill(victimName);
        KillsayEvents.fireOnDone(victimName);
    }

    private static PlayerEntity findPlayerByName(MinecraftClient mc, String name) {
        if (mc == null || mc.world == null || name == null) {
            return null;
        }
        for (PlayerEntity pp : mc.world.getPlayers()) {
            if (name.equals(pp.getGameProfile().getName())) {
                return pp;
            }
        }
        return null;
    }
}
