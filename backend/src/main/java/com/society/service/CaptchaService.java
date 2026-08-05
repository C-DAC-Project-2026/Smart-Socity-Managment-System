package com.society.service;

import com.society.dto.AuthDTOs.CaptchaResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A tiny, dependency-free CAPTCHA: generates a simple math question
 * ("7 + 3 = ?"), keeps the answer server-side keyed by a random captchaId,
 * and lets callers verify a submitted answer once. This avoids needing
 * a Google reCAPTCHA site key/secret and any outbound network call, so
 * it works immediately in any environment without extra setup.
 *
 * Entries auto-expire after 5 minutes so the in-memory map can't grow
 * without bound.
 */
@Service
public class CaptchaService {

    private record Entry(String answer, Instant expiresAt) {}

    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public CaptchaResponse generate() {
        int a = 1 + random.nextInt(9);
        int b = 1 + random.nextInt(9);
        boolean add = random.nextBoolean();
        String question = add ? (a + " + " + b + " = ?") : ((a + b) + " - " + b + " = ?");
        int answer = add ? (a + b) : a;

        String captchaId = UUID.randomUUID().toString();
        store.put(captchaId, new Entry(String.valueOf(answer), Instant.now().plusSeconds(300)));
        cleanupExpired();

        return new CaptchaResponse(captchaId, question);
    }

    /** One-shot verification: correct or not, the entry is removed so it can't be replayed. */
    public boolean verify(String captchaId, String submittedAnswer) {
        if (captchaId == null || submittedAnswer == null) return false;
        Entry entry = store.remove(captchaId);
        if (entry == null || Instant.now().isAfter(entry.expiresAt())) return false;
        return entry.answer().trim().equalsIgnoreCase(submittedAnswer.trim());
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt()));
    }
}
