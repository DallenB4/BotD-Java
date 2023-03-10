package glue_gunner4.botd.bot;

import glue_gunner4.botd.config.EmojisConfig;
import glue_gunner4.botd.server.AfkPlayer;
import glue_gunner4.botd.server.BotdServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.InteractionType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InfoCommand extends ListenerAdapter {
    public static final SlashCommandData COMMAND = Commands.slash("info", "Displays information about the server.").addOption(OptionType.STRING, "type", "Type of information to display.", true, true);

    public static final String[] OPTIONS = new String[]{"time", "players"};

    public static void infoCommand(SlashCommandInteractionEvent event) {
        switch (event.getOption("type").getAsString()) {
            case "time" -> displayTime(event);
            case "players" -> displayPlayers(event);
        }
    }

    private static void displayPlayers(SlashCommandInteractionEvent event) {
        MinecraftServer server = BotdServer.server;
        EmbedBuilder embedBuilder = new EmbedBuilder();
        int playerCount = server.getCurrentPlayerCount();
        if (playerCount == 0) {
            embedBuilder.setColor(new Color(237, 66, 69));
            embedBuilder.setTitle("No players are currently online.");
            event.replyEmbeds(embedBuilder.build()).complete().deleteOriginal().queueAfter(10, TimeUnit.SECONDS);
            return;
        }
        embedBuilder.setColor(new Color(87, 242, 135));
        embedBuilder.setTitle(playerCount + " players online");

        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : players) {
            AfkPlayer afkPlayer = (AfkPlayer) player;
            String playerName = player.getName().getString();
            embedBuilder.addField(EmojisConfig.getEmojiFromName(playerName) + " " + playerName,
                    !afkPlayer.isAfk() ? "Active" : String.format("AFK for %.1f minutes", afkPlayer.getAfkTime() / 60d), true);
        }
        InteractionHook reply = event.replyEmbeds(embedBuilder.build()).complete();
        reply.deleteOriginal().queueAfter(1, TimeUnit.MINUTES);
    }

    private static void displayTime(SlashCommandInteractionEvent event) {
        try {
            final String DAY_URL = "https://i.ibb.co/4f0DmDX/2023-01-10-13-02-02.png";
            final String NIGHT_URL = "https://i.ibb.co/fvjDZ5C/2023-01-10-13-02-36.png";
            long timeOfDay = BotdServer.server.getOverworld().getTimeOfDay() % 24000L;
            int totalSecs = (int) ((timeOfDay + 6000) / 0.277777777777);
            int hours = totalSecs / 3600;
            int minutes = (totalSecs % 3600) / 60;
            int seconds = totalSecs % 60;
            Date date = new SimpleDateFormat("kkmmss").parse(String.format("%02d%02d%02d", hours, minutes, seconds));
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");

            MessageEmbed embed = new EmbedBuilder()
                    .addField("Time", sdf.format(date), false)
                    .addField("Ticks", String.valueOf(timeOfDay), false)
                    .setThumbnail((timeOfDay > 0 && timeOfDay < 12542) ? DAY_URL : NIGHT_URL)
                    .setColor(Colors.getColorFromTime(timeOfDay))
                    .build();
            InteractionHook reply = event.replyEmbeds(embed).complete();
            reply.deleteOriginal().queueAfter(1, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Colors {
        static final Color DAY = new Color(229, 222, 68);
        static final Color NIGHT = new Color(0, 26, 38);
        static final Color SUNRISE_SET = new Color(239, 129, 14);
        static final Color DUSK_DAWN = new Color(5, 55, 82);

        static Color getColorFromTime(long time) {
            if (time >= 1000 && time <= 11000) {
                return DAY;
            } else if (time >= 13000 && time <= 23000) {
                return NIGHT;
            } else if (time >= 0 || time <= 12000) {
                return SUNRISE_SET;
            } else if (time >= 23000 || time <= 13000) {
                return DUSK_DAWN;
            }
            return null;
        }
    }

}
