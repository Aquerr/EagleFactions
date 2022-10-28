package io.github.aquerr.eaglefactions.integrations;

public class IntegrationActivationResult
{
    boolean canActivate;
    String reason;

    public static IntegrationActivationResult success()
    {
        return new IntegrationActivationResult(true);
    }

    public static IntegrationActivationResult failure(String reason)
    {
        return new IntegrationActivationResult(false, reason);
    }

    private IntegrationActivationResult(final boolean canActivate)
    {
        this.canActivate = canActivate;
    }

    private IntegrationActivationResult(final boolean canActivate, final String reason)
    {
        this.canActivate = canActivate;
        this.reason = reason;
    }

    public boolean isCanActivate()
    {
        return canActivate;
    }

    public String getReason()
    {
        return reason;
    }
}
