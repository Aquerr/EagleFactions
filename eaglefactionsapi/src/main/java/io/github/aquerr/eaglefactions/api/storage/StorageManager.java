package io.github.aquerr.eaglefactions.api.storage;

public interface StorageManager
{
    /**
     * Reloads storage. If any changes have been made in the files or database manually then this method loads all data into the storage cache.
     */
    void reloadStorage();
}
