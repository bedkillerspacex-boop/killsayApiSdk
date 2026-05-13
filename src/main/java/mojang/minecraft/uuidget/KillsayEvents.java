package mojang.minecraft.uuidget;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class KillsayEvents {
    private static final List<UserKill> userKillListeners = new CopyOnWriteArrayList<>();
    private static final List<KillStatusListener> statusListeners = new CopyOnWriteArrayList<>();

    private KillsayEvents() {}

    public static void registerUserKill(UserKill listener) {
        if (!userKillListeners.contains(listener)) {
            userKillListeners.add(listener);
        }
    }

    public static void unregisterUserKill(UserKill listener) {
        userKillListeners.remove(listener);
    }

    public static void registerStatusListener(KillStatusListener listener) {
        if (!statusListeners.contains(listener)) {
            statusListeners.add(listener);
        }
    }

    public static void unregisterStatusListener(KillStatusListener listener) {
        statusListeners.remove(listener);
    }

    static void fireUserKill(String victimName) {
        for (UserKill listener : userKillListeners) {
            try {
                listener.onUserKill(victimName);
            } catch (Throwable ignored) {
            }
        }
    }

    static void fireOnCooldownStart(long cooldownMs) {
        for (KillStatusListener listener : statusListeners) {
            try {
                listener.onCooldownStart(cooldownMs);
            } catch (Throwable ignored) {
            }
        }
    }

    static void fireOnDone(String victimName) {
        for (KillStatusListener listener : statusListeners) {
            try {
                listener.onDone(victimName);
            } catch (Throwable ignored) {
            }
        }
    }

    static void fireOnDeath() {
        for (KillStatusListener listener : statusListeners) {
            try {
                listener.onDeath();
            } catch (Throwable ignored) {
            }
        }
    }

    static void fireOnVictory() {
        for (KillStatusListener listener : statusListeners) {
            try {
                listener.onVictory();
            } catch (Throwable ignored) {
            }
        }
    }

    public static boolean isEnabled() {
        return ClientInitializer.isEnabled();
    }

    public static long getCooldownRemainingMs() {
        return ClientInitializer.getCooldownRemainingMs();
    }

    public static boolean isDeadPause() {
        return ClientInitializer.isDeadPause();
    }

    public static boolean isVictory() {
        return ClientInitializer.isVictory();
    }

    public static String getDoneName() {
        return ClientInitializer.getDoneName();
    }

    public static List<PlayerTrackingInfo> getTrackedPlayers() {
        return ClientInitializer.snapshotTrackedPlayers();
    }

    public static ConfigSnapshot getConfig() {
        return ClientInitializer.getConfigSnapshot();
    }

    @FunctionalInterface
    public interface UserKill {
        void onUserKill(String victimName);
    }

    public interface KillStatusListener {
        default void onCooldownStart(long cooldownMs) {}
        default void onDone(String victimName) {}
        default void onDeath() {}
        default void onVictory() {}
    }

    public record PlayerTrackingInfo(String name, long windowDeadline, String source, float predictedHealth) {}

    public record ConfigSnapshot(
            boolean enabled,
            boolean chatProjectileDetect,
            boolean transferGuard,
            double windowSeconds,
            double cooldownSeconds
    ) {}
}
