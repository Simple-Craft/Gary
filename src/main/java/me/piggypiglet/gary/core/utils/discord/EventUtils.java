package me.piggypiglet.gary.core.utils.discord;

import me.piggypiglet.gary.core.objects.enums.EventsEnum;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

// ------------------------------
// Copyright (c) PiggyPiglet 2018
// https://www.piggypiglet.me
// ------------------------------
public final class EventUtils {
    private static Event pullEvent(EventsEnum event, JDA jda) {
        final Logger logger = LoggerFactory.getLogger("Event Puller");
        final AtomicReference<Event> pulledEvent = new AtomicReference<>();

        EventListener listener = l -> {
            if (EventsEnum.fromEvent(l) == event) {
                pulledEvent.set(l);
            }
        };

        logger.info("Adding temporary event listener for " + event.name());
        jda.addEventListener(listener);

        while (pulledEvent.get() == null) try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }

        jda.removeEventListener(listener);
        logger.info("Removing temporary event listener for " + event.name());

        return pulledEvent.get();
    }

    public static Future<Message> pullMessage(TextChannel channel, User user) {
        CompletableFuture<Message> future = new CompletableFuture<>();
        GuildMessageReceivedEvent e = (GuildMessageReceivedEvent) pullEvent(EventsEnum.MESSAGE_CREATE, channel.getJDA());

        // Have to add the extra isbot check for some reason
        while (e.getChannel() != channel && e.getAuthor() != user && !e.getAuthor().isBot()) {
            e = (GuildMessageReceivedEvent) pullEvent(EventsEnum.MESSAGE_CREATE, channel.getJDA());
        }

        future.complete(e.getMessage());

        return future;
    }

    public static Future<MessageReaction> pullReaction(Message message, User user) {
        JDA jda = message.getJDA();
        CompletableFuture<MessageReaction> future = new CompletableFuture<>();
        GuildMessageReactionAddEvent e = (GuildMessageReactionAddEvent) pullEvent(EventsEnum.MESSAGE_REACTION_ADD, jda);

        while (message.getChannel().getMessageById(e.getMessageId()).complete() != message && e.getUser() != user) {
            e = (GuildMessageReactionAddEvent) pullEvent(EventsEnum.MESSAGE_REACTION_ADD, jda);
        }

        future.complete(e.getReaction());

        return future;
    }
}