package mojang.minecraft.uuidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ReadableScoreboardScore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HealthTracker {
    private static final Map<UUID, Float> predictedHealthMap = new HashMap<>();

    public static void onEntityStatus(int entityId, byte status) {
        if (status != 2) return; // Entity status 2 is "hurt" (red flash)

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Entity entity = mc.world.getEntityById(entityId);
        if (!(entity instanceof PlayerEntity victim)) return;

        // Only simulate if we are tracking this player (we attacked them)
        if (!ClientInitializer.isTracking(victim.getUuid())) return;

        float simulatedDamage = calculateSimulatedDamage(mc.player, victim);
        
        float currentPredicted = predictedHealthMap.getOrDefault(victim.getUuid(), getScoreboardHealth(victim));
        float nextPredicted = currentPredicted - simulatedDamage;
        
        // Clamp by scoreboard integer value
        nextPredicted = clampByScoreboard(victim, nextPredicted);
        
        predictedHealthMap.put(victim.getUuid(), nextPredicted);
    }

    private static float calculateSimulatedDamage(PlayerEntity attacker, PlayerEntity victim) {
        // Base attack damage
        double damage = attacker.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        
        // Armor and Toughness
        double armor = victim.getAttributeValue(EntityAttributes.ARMOR);
        double toughness = victim.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS);
        
        // Damage formula in 1.21 (simplified but following the requested logic)
        // damage = damage * (1 - min(20, max(armor / 5, armor - damage / (toughness / 4 + 2))) / 25)
        double f = 2.0 + toughness / 4.0;
        double g = Math.min(20.0, Math.max(armor / 5.0, armor - damage / f)) / 25.0;
        return (float) (damage * (1.0 - g));
    }

    private static float getScoreboardHealth(PlayerEntity player) {
        Scoreboard scoreboard = player.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
        if (objective == null) return 20.0f;
        
        ReadableScoreboardScore score = scoreboard.getScore(player, objective);
        if (score == null) return 20.0f;
        
        return (float) score.getScore();
    }

    private static float clampByScoreboard(PlayerEntity player, float simulatedHealth) {
        Scoreboard scoreboard = player.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
        if (objective == null) return simulatedHealth;

        ReadableScoreboardScore scoreObj = scoreboard.getScore(player, objective);
        if (scoreObj == null) return simulatedHealth;
        
        int score = scoreObj.getScore();
        
        // Integer Anchor: Health must be in [score, score + 1)
        if (simulatedHealth < (float) score) return (float) score;
        if (simulatedHealth >= (float) (score + 1)) return (float) score + 0.99f;
        
        return simulatedHealth;
    }

    public static float getPredictedHealth(UUID uuid) {
        return predictedHealthMap.getOrDefault(uuid, -1.0f);
    }
    
    public static void clear() {
        predictedHealthMap.clear();
    }
}
