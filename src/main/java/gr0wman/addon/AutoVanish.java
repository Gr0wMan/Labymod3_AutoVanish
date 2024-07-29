package gr0wman.addon;

import java.awt.*;
import java.util.List;
import net.labymod.api.LabyModAddon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.events.client.chat.MessageReceiveEvent;
import net.labymod.api.event.events.client.chat.MessageSendEvent;
import net.labymod.api.event.events.client.gui.RenderGameOverlayEvent;
import net.labymod.api.event.events.network.server.ServerSwitchEvent;
import net.labymod.core.LabyModCore;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class AutoVanish extends LabyModAddon {

  private  boolean AutoVanishEnabled = false;
  private boolean VanishEnabled = false;
  private boolean VanishStatusEnabled = true;

  private int VXCoords = 0;
  private int VYCoords = 0;
  private int VCustomColor = 0x0;
  
  private boolean SaveCfg = false;

  @Override
  public void onEnable() {
    getApi().getEventService().registerListener(this);
  }

  @Override
  public void loadConfig() {
    AutoVanishEnabled = getConfig().has("auto_vanish") ? getConfig().get("auto_vanish").getAsBoolean() : false;
    VanishStatusEnabled = getConfig().has("vanish_status_enabled") ? getConfig().get("vanish_status_enabled").getAsBoolean() : true;
    VXCoords = getConfig().has("vanishx") ? getConfig().get("vanishx").getAsInt() : 0;
    VYCoords = getConfig().has("vanishy") ? getConfig().get("vanishy").getAsInt() : 0;
    VCustomColor = getConfig().has("vanish_custom_color") ? getConfig().get("vanish_custom_color").getAsInt() : 0x0;

    SaveCfg();
  }

  @Override
  protected void fillSettings(List<SettingsElement> list) {
  }

  @Subscribe
  public void SaveCfgCheck(RenderGameOverlayEvent event) {
    if (!SaveCfg) {
      return;
    }

    SaveCfg = false;

    AutoVanish.this.getConfig().addProperty("auto_vanish", AutoVanishEnabled);
    AutoVanish.this.getConfig().addProperty("vanish_status_enabled", VanishStatusEnabled);
    AutoVanish.this.getConfig().addProperty("vanishx", VXCoords);
    AutoVanish.this.getConfig().addProperty("vanishy", VYCoords);
    AutoVanish.this.getConfig().addProperty("vanish_custom_color", VCustomColor);

    AutoVanish.this.saveConfig();
  }

  public void SaveCfg() {
    SaveCfg = true;
  }

  @Subscribe
  public void onRender(RenderGameOverlayEvent event) {
    if (VanishStatusEnabled) {
      DrawString(event, "Статус ваниша: ", VXCoords, VYCoords, VCustomColor == 0x0 ? Rainbow(300) : VCustomColor);
      if (VanishEnabled) {
        DrawString(event, "ВКЛЮЧЁН", VXCoords + 85, VYCoords, Color.green.getRGB());
      } else {
        DrawString(event, "ВЫКЛЮЧЕН", VXCoords + 85, VYCoords, Color.red.getRGB());
      }
    }
  }

  @Subscribe
  public void OnUpdate(MessageSendEvent event) {
    if (event.getMessage().equals(".vanish")) {
      event.setCancelled(true);

      AutoVanishEnabled = !AutoVanishEnabled;

      ClientMessage(TextFormatting.YELLOW + "Автоматический ваниш " + (AutoVanishEnabled ? TextFormatting.GREEN + "ВКЛЮЧЁН" : TextFormatting.RED + "ВЫКЛЮЧЕН"));

      SaveCfg();

    } else if (event.getMessage().equals(".status")) {
      event.setCancelled(true);

      VanishStatusEnabled = !VanishStatusEnabled;

      ClientMessage(TextFormatting.YELLOW + "Отображение состояния ваниша " + (VanishStatusEnabled ? TextFormatting.GREEN + "ВКЛЮЧЕНО" : TextFormatting.RED + "ВЫКЛЮЧЕНО"));

      SaveCfg();
    }
    else if (event.getMessage().startsWith(".coords")) {
      event.setCancelled(true);

      String[] messageSplit = event.getMessage().split(" ", 3);

      if (messageSplit.length == 1) {
        ClientMessage(TextFormatting.RED + "Вы не указали X и Y координаты!");
        return;
      } else if (messageSplit.length == 2) {
        ClientMessage(TextFormatting.RED + "Вы не указали Y координату!");
        return;
      }

      String xText = messageSplit[1];
      String yText = messageSplit[2];
      boolean isXCorrect = CheckCorrectInt(xText);
      boolean isYCorrect = CheckCorrectInt(yText);

      if (!isXCorrect || !isYCorrect) {
        if (!isXCorrect && !isYCorrect)
          ClientMessage(TextFormatting.RED + "Некорректные X и Y координаты!");
        else if (!isXCorrect)
          ClientMessage(TextFormatting.RED + "Некорректная X координата!");
        else if (!isYCorrect)
          ClientMessage(TextFormatting.RED + "Некорректная Y координата!");
        return;
      }

      VXCoords = Integer.parseInt(xText);
      VYCoords = Integer.parseInt(yText);

      ClientMessage(TextFormatting.GREEN + "Успешно применено!");

      SaveCfg();

    } else if (event.getMessage().startsWith(".color")) {
      event.setCancelled(true);

      String[] messageSplit = event.getMessage().split(" ", 2);

      if (messageSplit.length == 1) {
        ClientMessage(TextFormatting.RED + "Вы не указали айди цвета!");
      }

      String stringColor = "0x" + messageSplit[1];
      int intColor;
      {
        try {
          intColor = Integer.decode(stringColor);
        } catch (NumberFormatException e) {
          ClientMessage(TextFormatting.RED + "Некорректный цветовой код!");
          return;
        }
      }

      VCustomColor = intColor;

      ClientMessage(TextFormatting.GREEN + "Успешно применено!");

      SaveCfg();
    }
  }

  @Subscribe
  public void VanishChecker(MessageReceiveEvent event) {
    String[] messageSplit = event.getComponent().getString().split(" ");
    if (messageSplit[0].equals("Скрытие") && messageSplit[2].equals("включен")) {
      VanishEnabled = true;
    } else if (messageSplit[0].equals("Скрытие") && messageSplit[2].equals("выключено")) {
      VanishEnabled = false;
    }
  }

  @Subscribe
  public void OnServerSwitchEvent(ServerSwitchEvent event) {
    if (AutoVanishEnabled) {
      ChatMessage("/v");
    }
  }

  @Subscribe
  public void OnMessageSend(MessageSendEvent event) {
    if (event.getMessage().startsWith("/hub") || event.getMessage().startsWith("/рги")) {
      VanishEnabled = false;
    }
  }

  public void DrawString(RenderGameOverlayEvent event, String text, int x, int y, int color) {
    LabyModCore.getMinecraft().getFontRenderer().drawString(event.getMatrixStack(), text, x, y, color);
  }

  public int Rainbow(int delay) {
    double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20.0D);
    rainbowState %= 360.0D;
    return Color.getHSBColor((float) (rainbowState / 360.0D), 0.5F, 1.0F).getRGB();
  }

  public void ChatMessage(String message) {
    LabyModCore.getMinecraft().getPlayer().sendChatMessage(message);
  }

  public void ClientMessage(String message) {
    Minecraft.getInstance().player.sendMessage(new StringTextComponent(message), null);
  }

  public boolean CheckCorrectInt(String value) {
    int newValue = 0;
    {
      try {
        newValue = Integer.parseInt(value);
        return (true);
      } catch (NumberFormatException e) {
        return (false);
      }
    }
  }
}
