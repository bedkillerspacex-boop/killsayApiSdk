package mojang.minecraft.uuidget;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public final class ClientOptions {
    private static final Path PROPS_FILE =
            FabricLoader.getInstance().getConfigDir().resolve("killsay-api-sdk.properties");

    public static final double SIMPLE_DEFAULT_EXPAND = 0.35;
    public static final double TRACKING_DEFAULT_EXPAND = 0.5;

    private static boolean enabled = true;
    private static double cooldownSeconds = 3.0;
    private static double windowSeconds = 5.0;
    private static boolean chatProjectileDetect = false;
    private static boolean transferGuard = true;
    private static boolean debug = false;
    private static double projectileConfidence = -1.0;

    private ClientOptions() {}

    public static boolean enabled() { return enabled; }
    public static double cooldownSeconds() { return cooldownSeconds; }
    public static double windowSeconds() { return windowSeconds; }
    public static boolean chatProjectileDetect() { return chatProjectileDetect; }
    public static boolean transferGuard() { return transferGuard; }
    public static boolean debug() { return debug; }

    public static double simpleExpand() {
        return projectileConfidence < 0 ? SIMPLE_DEFAULT_EXPAND : projectileConfidence;
    }

    public static double trackingExpand() {
        return projectileConfidence < 0 ? TRACKING_DEFAULT_EXPAND : projectileConfidence + 0.15;
    }

    public static void load() {
        Properties p = new Properties();
        if (Files.exists(PROPS_FILE)) {
            try (var in = Files.newBufferedReader(PROPS_FILE)) {
                p.load(in);
            } catch (IOException ignored) {
            }
        }
        enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
        chatProjectileDetect = Boolean.parseBoolean(p.getProperty("chatProjectileDetect", "false"));
        transferGuard = Boolean.parseBoolean(p.getProperty("transferGuard", "true"));
        debug = Boolean.parseBoolean(p.getProperty("debug", "false"));
        try {
            cooldownSeconds = Math.max(0, Double.parseDouble(p.getProperty("cooldownSeconds", "3")));
        } catch (NumberFormatException e) {
            cooldownSeconds = 3.0;
        }
        try {
            windowSeconds = Math.max(0.5, Double.parseDouble(p.getProperty("windowSeconds", "5")));
        } catch (NumberFormatException e) {
            windowSeconds = 5.0;
        }
        String pc = p.getProperty("projectileConfidence", "default");
        if ("default".equalsIgnoreCase(pc) || pc.isBlank()) {
            projectileConfidence = -1.0;
        } else {
            try {
                double v = Double.parseDouble(pc);
                projectileConfidence = v < 0 ? -1.0 : Math.max(0.05, Math.min(1.0, v));
            } catch (NumberFormatException e) {
                projectileConfidence = -1.0;
            }
        }
        save();
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("enabled", Boolean.toString(enabled));
        p.setProperty("chatProjectileDetect", Boolean.toString(chatProjectileDetect));
        p.setProperty("transferGuard", Boolean.toString(transferGuard));
        p.setProperty("debug", Boolean.toString(debug));
        p.setProperty("cooldownSeconds", Double.toString(cooldownSeconds));
        p.setProperty("windowSeconds", Double.toString(windowSeconds));
        p.setProperty("projectileConfidence",
                projectileConfidence < 0 ? "default" : Double.toString(projectileConfidence));
        try (var out = Files.newBufferedWriter(PROPS_FILE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            p.store(out, "killsayApiSdk settings");
        } catch (IOException ignored) {
        }
    }
}
