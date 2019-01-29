package io.github.aquerr.eaglefactions.config;

import java.util.List;

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

    List<String> getListOfStrings(List<String> defaultValue, Object... nodePath);

    boolean setListOfStrings(List<String> listOfStrings, Object... nodePath);
}
