package botmanager.gitmanager.commands;

import botmanager.JDAUtils;
import botmanager.gitmanager.GitManager;
import botmanager.gitmanager.generic.GitManagerCommandBase;
import botmanager.gitmanager.objects.Task;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

/**
 *
 * @author MC_2018 <mc2018.git@gmail.com>
 */
public class TaskDescriptionCommand extends GitManagerCommandBase {

    private String[] KEYWORDS = {
        bot.getPrefix() + "task description",
        bot.getPrefix() + "task desc",
        bot.getPrefix() + "task d"
    };
    
    public TaskDescriptionCommand(GitManager bot) {
        super(bot);
    }

    @Override
    public void run(Event genericEvent) {
        GuildMessageReceivedEvent guildEvent = null;
        PrivateMessageReceivedEvent privateEvent = null;
        User user;
        Task task;
        String input;
        long guildID;
        int taskNumber;
        boolean found = false;

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            guildEvent = (GuildMessageReceivedEvent) genericEvent;
            input = guildEvent.getMessage().getContentRaw();
            user = guildEvent.getAuthor();
            guildID = guildEvent.getGuild().getIdLong();
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            privateEvent = (PrivateMessageReceivedEvent) genericEvent;
            input = privateEvent.getMessage().getContentRaw();
            user = privateEvent.getAuthor();
            guildID = bot.readUserSettings(user.getIdLong()).getDefaultGuildID();
        } else {
            return;
        }

        for (String keyword : KEYWORDS) {
            if (input.toLowerCase().startsWith(keyword + " ")) {
                input = input.substring(keyword.length() + 1, input.length());
                found = true;
                break;
            } else if (input.toLowerCase().replaceAll(" ", "").equals(keyword.replaceAll(" ", ""))) {
                JDAUtils.sendPrivateMessage(user, getFailureEmbed());
            }
        }

        if (!found) {
            return;
        } else if (guildEvent != null && !bot.isTaskChannel(guildEvent.getChannel())) {
            guildEvent.getMessage().delete().queue();
        }
        
        try {
            taskNumber = Integer.parseInt(input.split(" ")[0]);
        } catch (Exception e) {
            JDAUtils.sendPrivateMessage(user, getFailureEmbed());
            return;
        }
        
        if (input.split(" ").length < 2) {
            JDAUtils.sendPrivateMessage(user, getFailureEmbed());
            return;
        }
        
        input = input.substring(input.split(" ")[0].length() + 1, input.length());
        task = bot.readTask(guildID, taskNumber);
        task.setDescription(input, user.getIdLong());
        bot.getTaskChannel(task.getGuildID(), task.getStatus()).editMessageById(task.getMessageID(), bot.generateTaskEmbed(task)).queue();
        bot.writeTask(task);
    }

    
    @Override
    public Field info() {
        return new Field("Changing a Task Description", "```" + KEYWORDS[0] + " 102 New Description```", false);
    }
    
    @Override
    public MessageEmbed getFailureEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        
        eb.addField(
                "Command Failed",
                "Please use proper syntax:\n"
                        + "```" + KEYWORDS[0] + " TASK_ID NEW_DESCRIPTION```",
                true);
        
        return eb.build();
    }

    
}
