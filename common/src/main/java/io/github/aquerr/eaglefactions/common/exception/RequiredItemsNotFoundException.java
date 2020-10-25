package io.github.aquerr.eaglefactions.common.exception;

public class RequiredItemsNotFoundException extends Exception
{
    public RequiredItemsNotFoundException()
    {

    }

    public RequiredItemsNotFoundException(String s)
    {
        super(s);
    }

    public RequiredItemsNotFoundException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}
