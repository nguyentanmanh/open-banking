package com.manh.openbanking.infrastructure.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageResolver {
    private final MessageSource messageSource;

    public MessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String resolve(String key, Locale locale) {
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (NoSuchMessageException ignored) {
            try {
                return messageSource.getMessage(key, null, Locale.ENGLISH);
            } catch (NoSuchMessageException missing) {
                return key;
            }
        }
    }
}
