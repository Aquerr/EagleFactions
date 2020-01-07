package io.github.aquerr.eaglefactions.common.message;

import java.util.List;

public class EFMessages
{
//    public static <T> EFMessage<T> THERE_IS_NO_FACTION_CALLED_FACTION_NAME = new EFMessage(Placeholders.FACTION_NAME);


    public static class EFMessage<T>
    {
        private final List<String> placeholders;
        private final String rawMessage;

        public EFMessage(final String rawMessage, final List<String> placeholders)
        {
            this.rawMessage = rawMessage;
            this.placeholders = placeholders;
        }

//        public Text getMessage(T supplier)
//        {
//
//        }
    }
}