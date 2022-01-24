package io.github.aquerr.eaglefactions.util.resource;

import java.net.URL;

public final class ResourceUtils
{
    public static Resource getResource(String path)
    {
        URL url = getResourceURL(path);
        if (url == null)
            return null;
        return new Resource(path, url);
    }

    private static URL getResourceURL(String fileName)
    {
        return ResourceUtils.class.getClassLoader().getResource(fileName);
    }

    private ResourceUtils()
    {

    }
}
