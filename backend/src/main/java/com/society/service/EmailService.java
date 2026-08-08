package com.society.service;

/**
 * Thin wrapper around JavaMailSender used by every "send a notification email"
 * flow in the app (society registered, society approved, bill generated,
 * notice published). Centralised here so:
 *   1. every email flow fails the same safe way (logs and moves on instead of
 *      throwing and rolling back the real action - e.g. a bill must still be
 *      created even if the mail server is temporarily down), and
 *   2. sending to many recipients (e.g. all residents of a society) happens
 *      one message at a time so one bad address doesn't stop the rest.
 */
public interface EmailService {
    void send(String to, String subject, String body);
    void sendToAll(Iterable<String> recipients, String subject, String body);
}
