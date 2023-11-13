package io.github.aquerr.eaglefactions.version;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aquerr on 2018-02-21.
 */

public final class VersionChecker
{
    private static final String USER_AGENT = "Mozilla/5.0";

    private static class InstanceHolder
    {
        public static VersionChecker INSTANCE = null;
    }

    public static VersionChecker getInstance()
    {
        if (InstanceHolder.INSTANCE == null)
            InstanceHolder.INSTANCE = new VersionChecker();
        return InstanceHolder.INSTANCE;
    }

    public boolean isLatest(String version)
    {
        try
        {
            Sponge.getServer().getConsole().sendMessage(Text.of("Checking if " + version + " is the latest Eagle Factions version..."));
            Gson gson = new Gson();
            //TODO: Remove this when Ore API v2 will be able to handle session-less authentication for public endpoints.
            String session = getSession(gson);
            Date latestVersionDate = getLatestVersionDate(gson, session);
            Date currentVersionDate = getCurrentVersionDate(gson, session, version);
            return currentVersionDate.before(latestVersionDate);
        }
        catch (Exception exception)
        {
            Sponge.getServer().getConsole().sendMessage(Text.of("Could not check if there is a new version of Eagle Factions available. Reason: " + exception.getMessage()));
            Sponge.getServer().getConsole().sendMessage(Text.of("Considering current version as LATEST."));
            return true;
        }
    }

    private Date getCurrentVersionDate(Gson gson, String session, String currentVersion)
    {
        String url = "https://ore.spongepowered.org/api/v2/projects/eaglefactions/versions/" + currentVersion;
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", USER_AGENT);
        headers.put("authorization", "OreApi session=" + session);
        JsonObject jsonObject = sendRequest(gson, "GET", url, headers);
        if (jsonObject == null)
            return null;
        String stringDate = jsonObject.get("created_at").getAsString();
        return Date.from(Instant.parse(stringDate));
    }

    private Date getLatestVersionDate(Gson gson, String session)
    {
        String url = "https://ore.spongepowered.org/api/v2/projects/eaglefactions/versions?offset=0";
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", USER_AGENT);
        headers.put("authorization", "OreApi session=" + session);
        JsonObject jsonObject = sendRequest(gson, "GET", url, headers);
        String stringDate = jsonObject.get("result").getAsJsonArray().get(0).getAsJsonObject().get("created_at").getAsString();
        return Date.from(Instant.parse(stringDate));
    }

    private String getSession(Gson gson)
    {
        String url = "https://ore.spongepowered.org/api/v2/authenticate";
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", USER_AGENT);
        JsonObject jsonObject = sendRequest(gson, "POST", url, headers);
        return jsonObject.get("session").getAsString();
    }

    private JsonObject sendRequest(Gson gson, String method, String request, Map<String, String> headers)
    {
        try
        {
            URL url = new URL(request);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            headers.forEach(connection::setRequestProperty);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                try(InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    JsonReader jsonReader = new JsonReader(bufferedReader))
                {
                    return gson.fromJson(jsonReader, JsonObject.class);
                }
            }

        }
        catch (IOException e)
        {
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "Couldn't lookup if there is a new version of Eagle Factions available. Reason: " + e.getMessage()));
        }

        return null;
    }
}
