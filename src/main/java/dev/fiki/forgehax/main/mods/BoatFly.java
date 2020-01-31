package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.ForgeHaxHooks;
import dev.fiki.forgehax.common.events.RenderBoatEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.ClientTickEvent;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.util.MovementInput;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class BoatFly extends ToggleMod {
  
  public final Setting<Double> speed =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("speed")
          .description("how fast to move")
          .defaultTo(5.0D)
          .build();
  /*public final Setting<Double> maintainY = getCommandStub().builders().<Double>newSettingBuilder()
  .name("YLevel").description("automatically teleport back up to this Y level").defaultTo(0.0D).build();*/
  public final Setting<Double> speedY =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("FallSpeed")
          .description("how slowly to fall")
          .defaultTo(0.033D)
          .build();
  
  public final Setting<Boolean> setYaw =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("SetYaw")
          .description("set the boat yaw")
          .defaultTo(true)
          .build();
  public final Setting<Boolean> noClamp =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("NoClamp")
          .description("clamp view angles")
          .defaultTo(true)
          .build();
  public final Setting<Boolean> noGravity =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("NoGravity")
          .description("disable boat gravity")
          .defaultTo(true)
          .build();
  
  public BoatFly() {
    super(Category.MISC, "BoatFly", false, "Boathax");
  }
  
  @SubscribeEvent // disable gravity
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    ForgeHaxHooks.isNoBoatGravityActivated =
        Globals.getMountedEntity() instanceof BoatEntity; // disable gravity if in boat
  }
  
  @Override
  public void onDisabled() {
    // ForgeHaxHooks.isNoClampingActivated = false; // disable view clamping
    ForgeHaxHooks.isNoBoatGravityActivated = false; // disable gravity
    ForgeHaxHooks.isBoatSetYawActivated = false;
    // ForgeHaxHooks.isNotRowingBoatActivated = false; // items always usable - can not be disabled
  }
  
  @Override
  public void onLoad() {
    ForgeHaxHooks.isNoClampingActivated = noClamp.getAsBoolean();
  }
  
  @SubscribeEvent
  public void onRenderBoat(RenderBoatEvent event) {
    if (EntityUtils.isDrivenByPlayer(event.getBoat()) && setYaw.getAsBoolean()) {
      float yaw = Globals.getLocalPlayer().rotationYaw;
      event.getBoat().rotationYaw = yaw;
      event.setYaw(yaw);
    }
  }
  
  @SubscribeEvent
  public void onClientTick(ClientTickEvent.Pre event) {
    // check if the player is really riding a entity
    if (Globals.getLocalPlayer() != null && Globals.getMountedEntity() != null) {
      
      ForgeHaxHooks.isNoClampingActivated = noClamp.getAsBoolean();
      ForgeHaxHooks.isNoBoatGravityActivated = noGravity.getAsBoolean();
      ForgeHaxHooks.isBoatSetYawActivated = setYaw.getAsBoolean();

      double velX, velY, velZ;
      
      if (Globals.getGameSettings().keyBindJump.isKeyDown()) {
        // trick the riding entity to think its onground
        Globals.getMountedEntity().onGround = false;

        // teleport up
        velY = Globals.getGameSettings().keyBindSprint.isKeyDown() ? 5.D : 1.5D;
      } else {
        velY = Globals.getGameSettings().keyBindSprint.isKeyDown() ? -1.0 : -speedY.getAsDouble();
      }

      MovementInput movementInput = Globals.getLocalPlayer().movementInput;
      double forward = movementInput.moveForward;
      double strafe = movementInput.moveStrafe;
      float yaw = Globals.getLocalPlayer().rotationYaw;

      if ((forward == 0.0D) && (strafe == 0.0D)) {
        velX = velZ = 0.D;
      } else {
        if (forward != 0.0D) {
          if (strafe > 0.0D) {
            yaw += (forward > 0.0D ? -45 : 45);
          } else if (strafe < 0.0D) {
            yaw += (forward > 0.0D ? 45 : -45);
          }

          strafe = 0.0D;

          if (forward > 0.0D) {
            forward = 1.0D;
          } else if (forward < 0.0D) {
            forward = -1.0D;
          }
        }

        double sin = Math.sin(Math.toRadians(yaw + 90.0F));
        double cos = Math.cos(Math.toRadians(yaw + 90.0F));

        velX = (forward * speed.get() * cos + strafe * speed.get() * sin);
        velZ = (forward * speed.get() * sin - strafe * speed.get() * cos);
      }

      Globals.getMountedEntity().setMotion(velX, velY, velZ);
    }
  }
}