package io.github.aquerr.eaglefactions.api.config;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IConfiguration
{
    ConfigFields getConfigFields();
    void save();
    void reloadConfiguration();

    int getInt(int defaultValue, Object... nodePath);
    double getDouble(double defaultValue, Object... nodePath);
    float getFloat(float defaultValue, Object... nodePath);
    boolean getBoolean(boolean defaultValue, Object... nodePath);
    String getString(String defaultValue, Object... nodePath);

    List<String> getListOfStrings(Collection<String> defaultValue, Object... nodePath);
    Set<String> getSetOfStrings(Collection<String> defaultValue, Object... nodePath);

    boolean setListOfStrings(Collection<String> listOfStrings, Object... nodePath);
    boolean setSetOfStrings(Collection<String> setOfStrings, Object... nodePath);
}
