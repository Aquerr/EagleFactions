package io.github.aquerr.eaglefactions.style;

import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;

public interface StyleLayout
{
    TextColor getPassive();

    TextColor getError();

    PaginationList.Builder getPagination(Text title);

    TextColor getData();

    TextColor getBracket();

    Text getPlayer();

}
