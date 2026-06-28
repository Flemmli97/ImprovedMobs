package io.github.flemmli97.improvedmobs.neoforge.events;

import io.github.flemmli97.improvedmobs.common.commands.ImprovedMobsCommand;
import io.github.flemmli97.improvedmobs.common.events.EventCalls;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

public class EventHandler {

    @SubscribeEvent
    public void commands(RegisterCommandsEvent event) {
        ImprovedMobsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onEntityLoad(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof Mob mob) {
            EventCalls.onEntityLoad(mob);
        }
    }

    @SubscribeEvent
    public void hurtEvent(LivingDamageEvent.Pre e) {
        e.setNewDamage(EventCalls.hurtEvent(e.getEntity(), e.getSource(), e.getNewDamage()));
    }

    @SubscribeEvent
    public void attackEvent(LivingIncomingDamageEvent e) {
        if (EventCalls.onAttackEvent(e.getEntity(), e.getSource()))
            e.setCanceled(true);
    }

    @SubscribeEvent
    public void openTile(PlayerInteractEvent.RightClickBlock e) {
        EventCalls.openTile(e.getEntity(), e.getPos());
    }

    @SubscribeEvent
    public void equipPet(PlayerInteractEvent.EntityInteract e) {
        if (EventCalls.equipPet(e.getEntity(), e.getHand(), e.getTarget()))
            e.setCanceled(true);
    }

    @SubscribeEvent
    public void projectileImpact(ProjectileImpactEvent e) {
        if (e.getEntity() instanceof Projectile proj && EventCalls.projectileImpact(proj, e.getRayTraceResult())) {
            e.setCanceled(true);
        }
    }

    //Note: Sodium-Forge breaks this since they modify explosion but dont call the forge event
    @SubscribeEvent
    public void explosion(ExplosionEvent.Detonate event) {
        EventCalls.explosion(event.getExplosion(), event.getExplosion().getDirectSourceEntity(), event.getAffectedEntities());
    }

    @SubscribeEvent
    public void onTagUpdate(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            EventCalls.onTagReloaded();
        }
    }
}
