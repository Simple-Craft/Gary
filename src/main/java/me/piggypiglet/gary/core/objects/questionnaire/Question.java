package me.piggypiglet.gary.core.objects.questionnaire;

import emoji4j.EmojiUtils;
import lombok.Getter;
import me.piggypiglet.gary.core.objects.enums.QuestionType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

// ------------------------------
// Copyright (c) PiggyPiglet 2018
// https://www.piggypiglet.me
// ------------------------------
public final class Question {
    @Getter private String key;
    @Getter private String question;
    @Getter private QuestionType questionType;
    @Getter private List<Object> emotes;

    public Question(String key, String question, QuestionType questionType) {
        this.key = key;
        this.question = question;
        this.questionType = questionType;
    }

    /**
     * Set the emotes that will be added to the question for the user to choose from. This accepts unserialized emotes, emote objects, and unicodes.
     * @param jda Instance of Gary's JDA to serialize unserialized emotes.
     * @param emotes Emotes that will be added to the question.
     * @return Returns an instance of this question.
     */
    public Question setEmotes(JDA jda, Object... emotes) {
        this.emotes = new ArrayList<>();

        Arrays.stream(emotes).forEach(e -> {
            if (e instanceof Emote) {
                this.emotes.add(e);
            }

            if (e instanceof String) {
                String emote = (String) e;

                if (emote.startsWith("<:")) {
                    this.emotes.add(jda.getEmoteById(Stream.of(emote.replaceAll("^\\D+", "").split("\\D+"))
                            .mapToLong(Long::parseLong).max().orElse(-1L)));
                } else if (EmojiUtils.isEmoji(emote)) {
                    this.emotes.add(emote);
                }
            }
        });

        return this;
    }
}
