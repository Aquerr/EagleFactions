package io.github.aquerr.eaglefactions.version;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.spongepowered.api.Sponge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Date;

import static net.kyori.adventure.identity.Identity.nil;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

/**
 * Created by Aquerr on 2018-02-21.
 */

public class VersionChecker
{
    private static final String USER_AGENT = "Mozilla/5.0";

    public static boolean isLatest(String version)
    {
        String latest = "https://ore.spongepowered.org/api/v1/projects/eaglefactions/versions";
        String currentTag = "https://ore.spongepowered.org/api/v1/projects/eaglefactions/versions/" + version;

        String latestJsonData = sendRequest(latest);
        String currentJsonData = sendRequest(currentTag);

        if (latestJsonData != null && currentJsonData != null)
        {
            JsonParser parser = new JsonParser();
            JsonElement latestJsonElement = parser.parse(latestJsonData);
            JsonElement currentJsonElement = parser.parse(currentJsonData);

            if (latestJsonElement.isJsonArray())
            {
                JsonArray latestJsonArray = latestJsonElement.getAsJsonArray();
                JsonElement latestRelease = latestJsonArray.get(0);

                Date latestReleaseDate = Date.from(Instant.parse(latestRelease.getAsJsonObject().get("createdAt").getAsString()));
                Date currentReleaseDate = Date.from(Instant.parse(currentJsonElement.getAsJsonObject().get("createdAt").getAsString()));

                if (currentReleaseDate.before(latestReleaseDate)) return false;
            }
        }

        return true;
    }

    private static String sendRequest(String request)
    {
        try
        {
            URL url = new URL(request);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));

                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = bufferedReader.readLine()) != null)
                {
                    response.append(inputLine);
                }
                bufferedReader.close();

                return response.toString();
            }

        }
        catch (IOException e)
        {
            Sponge.server().sendMessage(nil(), text("Couldn't lookup if there is a new version of Eagle Factions available. Reason: " + e.getMessage(), RED));
        }

        return null;
    }
}
