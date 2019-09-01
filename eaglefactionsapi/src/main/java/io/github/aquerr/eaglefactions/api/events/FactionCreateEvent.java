package io.github.aquerr.eaglefactions.api.events;

public interface FactionCreateEvent extends FactionEvent
{
    /**
     * <strong>Currently unimplemented</strong>
     *
     * @return <tt>true</tt> if faction is being created by items or <tt>false</tt> if it is not
     */
    boolean isCreatedByItems();
}
