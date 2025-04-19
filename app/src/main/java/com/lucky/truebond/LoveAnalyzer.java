package com.lucky.truebond;

import java.util.List;
import java.util.regex.Pattern;

public class LoveAnalyzer {
    private static final Pattern POSITIVE_WORDS = Pattern.compile(
            "love|happy|joy|amazing|wonderful|beautiful|great|perfect|thank|appreciate|miss|care|support|understand|respect|trust|together|forever|sweet|kind|cute|adorable|amazing|awesome|best|blessed|brilliant|charming|delightful|excellent|fantastic|fabulous|glorious|gorgeous|incredible|lovely|magnificent|marvelous|outstanding|perfect|phenomenal|pleasant|pleasing|splendid|super|terrific|wonderful"
    );

    private static final Pattern NEGATIVE_WORDS = Pattern.compile(
            "hate|angry|sad|upset|disappointed|frustrated|annoyed|mad|irritated|bored|tired|exhausted|drained|stressed|worried|anxious|depressed|lonely|jealous|insecure|doubt|regret|sorry|apologize|forgive|forget|ignore|avoid|distance|separate|break|end|quit|stop|leave|gone|lost|hurt|pain|suffer|cry|tears|fear|scared|afraid|nervous|tense|pressure|overwhelm|confuse|conflict|fight|argue|disagree|complain|criticize|blame|accuse|judge|control|manipulate|deceive|lie|cheat|betray|abuse|insult|offend|hurt|harm|damage|destroy|ruin|waste|lose|fail|fall|break|crash|burn|die|dead|kill|murder|suicide"
    );

    public static LoveReport analyzeMessages(List<Message> messages) {
        if (messages.isEmpty()) {
            return new LoveReport(50, "NEUTRAL", "No messages today. Try to communicate more with your partner!", 0);
        }

        int positiveCount = 0;
        int negativeCount = 0;
        int totalMessages = messages.size();
        int coldZones = 0;
        int consecutiveNegative = 0;

        for (Message message : messages) {
            String text = message.getText().toLowerCase();
            boolean isPositive = POSITIVE_WORDS.matcher(text).find();
            boolean isNegative = NEGATIVE_WORDS.matcher(text).find();

            if (isPositive) positiveCount++;
            if (isNegative) negativeCount++;

            if (isNegative) {
                consecutiveNegative++;
                if (consecutiveNegative >= 3) {
                    coldZones++;
                    consecutiveNegative = 0;
                }
            } else {
                consecutiveNegative = 0;
            }
        }

        // Calculate love score (0-100)
        int loveScore = (int) ((positiveCount / (double) totalMessages) * 100);

        // Determine mood
        String mood;
        if (positiveCount > negativeCount * 2) {
            mood = "POSITIVE";
        } else if (negativeCount > positiveCount * 2) {
            mood = "NEGATIVE";
        } else {
            mood = "NEUTRAL";
        }

        // Generate suggestion
        String suggestion;
        if (loveScore >= 80) {
            suggestion = "Your communication is excellent! Keep up the positive vibes and continue expressing your love.";
        } else if (loveScore >= 60) {
            suggestion = "Good communication! Try to increase positive interactions and reduce negative ones.";
        } else if (loveScore >= 40) {
            suggestion = "Communication needs improvement. Focus on positive expressions and resolving conflicts.";
        } else {
            suggestion = "Serious attention needed. Consider having an open conversation about your relationship.";
        }

        return new LoveReport(loveScore, mood, suggestion, coldZones);
    }
} 