//package io.github.aquerr.eaglefactions.integrations.bstats;
//
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonPrimitive;
//import com.google.inject.Inject;
//import ninja.leaping.configurate.commented.CommentedConfigurationNode;
//import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
//import org.apache.commons.lang3.Validate;
//import org.slf4j.Logger;
//import org.spongepowered.api.Platform;
//import org.spongepowered.api.Sponge;
//import org.spongepowered.api.config.ConfigDir;
//import org.spongepowered.api.plugin.PluginContainer;
//import org.spongepowered.api.scheduler.Scheduler;
//import org.spongepowered.api.scheduler.Task;
//
//import javax.net.ssl.HttpsURLConnection;
//import java.io.*;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Path;
//import java.util.*;
//import java.util.concurrent.Callable;
//import java.util.concurrent.CompletableFuture;
//import java.util.zip.GZIPOutputStream;
//
///**
// * bStats collects some data for plugin authors.
// *
// * Check out https://bStats.org/ to learn more about bStats!
// */
//public class Metrics
//{
//    // The version deserialize this bStats class
//    public static final int B_STATS_VERSION = 1;
//
//    // The url to which the data is sent
//    private static final String URL = "https://bStats.org/submitData/sponge";
//
//    // The logger
//    private Logger logger;
//
//    // The plugin
//    private final PluginContainer plugin;
//
//    private static final int pluginId = 6831;
//
//    // Is bStats enabled on this server?
//    private boolean enabled;
//
//    // The uuid deserialize the server
//    private String serverUUID;
//
//    // Should failed requests be logged?
//    private boolean logFailedRequests = false;
//
//    // A list with all custom charts
//    private final List<CustomChart> charts = new ArrayList<>();
//
//    // The config path
//    private Path configDir;
//
//    // The timer task
//    private TimerTask timerTask;
//
//    // The constructor is not meant to be called by the user himself.
//    // The instance is created using Dependency Injection (https://docs.spongepowered.org/master/en/plugin/injection.html)
//    @Inject
//    private Metrics(PluginContainer plugin, Logger logger, @ConfigDir(sharedRoot = true) Path configDir)
//    {
//        this.plugin = plugin;
//        this.logger = logger;
//        this.configDir = configDir;
//
//        try
//        {
//            loadConfig();
//        }
//        catch (IOException e)
//        {
//            // Failed to load configuration
//            logger.warn("Failed to load bStats config!", e);
//            return;
//        }
//
//        // We are not allowed to send data about this server :(
//        if (!enabled)
//        {
//            return;
//        }
//
//        startSubmitting();
//    }
//
//    /**
//     * Adds a custom chart.
//     *
//     * @param chart The chart to add.
//     */
//    public void addCustomChart(CustomChart chart)
//    {
//        Validate.notNull(chart, "Chart cannot be null");
//        charts.add(chart);
//    }
//
//    /**
//     * Gets the plugin specific data.
//     * This method is called using Reflection.
//     *
//     * @return The plugin specific data.
//     */
//    public JsonObject getPluginData()
//    {
//        JsonObject data = new JsonObject();
//
//        String pluginName = plugin.getName();
//        String pluginVersion = plugin.getVersion().orElse("unknown");
//
//        data.addProperty("pluginName", pluginName);
//        data.addProperty("id", pluginId);
//        data.addProperty("pluginVersion", pluginVersion);
//
//        JsonArray customCharts = new JsonArray();
//        for (CustomChart customChart : charts) {
//            // Add the data of the custom charts
//            JsonObject chart = customChart.getRequestJsonObject(logger, logFailedRequests);
//            if (chart == null) { // If the chart is null, we skip it
//                continue;
//            }
//            customCharts.add(chart);
//        }
//        data.add("customCharts", customCharts);
//
//        return data;
//    }
//
//    private void startSubmitting()
//    {
//        // We use a timer cause want to be independent from the server tps
//        final Timer timer = new Timer(true);
//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                // The data collection (e.g. for custom graphs) is done sync
//                // Don't be afraid! The connection to the bStats server is still async, only the stats collection is sync ;)
//                Scheduler scheduler = Sponge.getScheduler();
//                Task.Builder taskBuilder = scheduler.createTaskBuilder();
//                taskBuilder.execute(() -> submitData()).submit(plugin);
//            }
//        };
//        timer.scheduleAtFixedRate(timerTask, 1000 * 60 * 5, 1000 * 60 * 30);
//        // Submit the data every 30 minutes, first time after 5 minutes to give other plugins enough time to start
//        // WARNING: Changing the frequency has no effect but your plugin WILL be blocked/deleted!
//        // WARNING: Just don't do it!
//    }
//
//    /**
//     * Gets the server specific data.
//     *
//     * @return The server specific data.
//     */
//    private JsonObject getServerData()
//    {
//        // Minecraft specific data
//        int playerAmount = Sponge.getServer().getOnlinePlayers().size();
//        playerAmount = playerAmount > 200 ? 200 : playerAmount;
//        int onlineMode = Sponge.getServer().getOnlineMode() ? 1 : 0;
//        String minecraftVersion = Sponge.getGame().getPlatform().getMinecraftVersion().getName();
//        String spongeImplementation = Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName();
//
//        // OS/Java specific data
//        String javaVersion = System.getProperty("java.version");
//        String osName = System.getProperty("os.name");
//        String osArch = System.getProperty("os.arch");
//        String osVersion = System.getProperty("os.version");
//        int coreCount = Runtime.getRuntime().availableProcessors();
//
//        JsonObject data = new JsonObject();
//
//        data.addProperty("serverUUID", serverUUID);
//
//        data.addProperty("playerAmount", playerAmount);
//        data.addProperty("onlineMode", onlineMode);
//        data.addProperty("minecraftVersion", minecraftVersion);
//        data.addProperty("spongeImplementation", spongeImplementation);
//
//        data.addProperty("javaVersion", javaVersion);
//        data.addProperty("osName", osName);
//        data.addProperty("osArch", osArch);
//        data.addProperty("osVersion", osVersion);
//        data.addProperty("coreCount", coreCount);
//
//        return data;
//    }
//
//    /**
//     * Collects the data and sends it afterwards.
//     */
//    private void submitData()
//    {
//        final JsonObject data = getServerData();
//
//        if (!Sponge.getMetricsConfigManager().areMetricsEnabled(this.plugin))
//            return;
//
//        JsonArray pluginData = new JsonArray();
//        JsonObject plugin = getPluginData();
//        pluginData.add(plugin);
//        data.add("plugins", pluginData);
//
//        // Create a new thread for the connection to the bStats server
//        CompletableFuture.runAsync(() ->
//        {
//            try
//            {
//                sendData(data);
//            }
//            catch (Exception e)
//            {
//                if (logFailedRequests) {
//                    logger.warn("Could not submit plugin stats!", e);
//                }
//            }
//        });
//    }
//
//    /**
//     * Loads the bStats configuration.
//     *
//     * @throws IOException If something did not work :(
//     */
//    private void loadConfig() throws IOException {
//        Path configPath = configDir.resolve("bStats");
//        configPath.toFile().mkdirs();
//        File configFile = new File(configPath.toFile(), "config.conf");
//        HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setFile(configFile).build();
//        CommentedConfigurationNode node;
//        if (!configFile.exists())
//        {
//            configFile.createNewFile();
//            node = configurationLoader.load();
//
//            // Add default values
//            node.getNode("enabled").setValue(true);
//            // Every server gets it's unique random id.
//            node.getNode("serverUuid").setValue(UUID.randomUUID().toString());
//            // Should failed request be logged?
//            node.getNode("logFailedRequests").setValue(false);
//
//            // Add information about bStats
//            node.getNode("enabled").setComment(
//                    "bStats collects some data for plugin authors like how many servers are using their plugins.\n" +
//                            "To honor their work, you should not disable it.\n" +
//                            "This has nearly no effect on the server performance!\n" +
//                            "Check out https://bStats.org/ to learn more :)"
//            );
//
//            configurationLoader.save(node);
//        }
//        else
//        {
//            node = configurationLoader.load();
//        }
//
//        // Load configuration
//        enabled = node.getNode("enabled").getBoolean(true);
//        serverUUID = node.getNode("serverUuid").getString();
//        logFailedRequests = node.getNode("logFailedRequests").getBoolean(false);
//    }
//
//    /**
//     * Reads the first line deserialize the file.
//     *
//     * @param file The file to read. Cannot be null.
//     * @return The first line deserialize the file or <code>null</code> if the file does not exist or is empty.
//     * @throws IOException If something did not work :(
//     */
//    private String readFile(File file) throws IOException
//    {
//        if (!file.exists())
//        {
//            return null;
//        }
//        try (FileReader fileReader = new FileReader(file);
//             BufferedReader bufferedReader =  new BufferedReader(fileReader))
//        {
//            return bufferedReader.readLine();
//        }
//    }
//
//    /**
//     * Writes a String to a file. It also adds a note for the user,
//     *
//     * @param file The file to write to. Cannot be null.
//     * @param text The text to write.
//     * @throws IOException If something did not work :(
//     */
//    private void writeFile(File file, String text) throws IOException
//    {
//        if (!file.exists())
//        {
//            file.createNewFile();
//        }
//        try (FileWriter fileWriter = new FileWriter(file);
//             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter))
//        {
//            bufferedWriter.write(text);
//            bufferedWriter.newLine();
//            bufferedWriter.write("Note: This class only exists for internal purpose. You can ignore it :)");
//        }
//    }
//
//    /**
//     * Sends the data to the bStats server.
//     *
//     * @param data The data to send.
//     * @throws Exception If the request failed.
//     */
//    private static void sendData(JsonObject data) throws Exception
//    {
//        Validate.notNull(data, "Data cannot be null");
//        HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();
//
//        // Compress the data to save bandwidth
//        byte[] compressedData = compress(data.toString());
//
//        // Add headers
//        connection.setRequestMethod("POST");
//        connection.addRequestProperty("Accept", "application/json");
//        connection.addRequestProperty("Connection", "close");
//        connection.addRequestProperty("Content-Encoding", "gzip"); // We gzip our request
//        connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
//        connection.setRequestProperty("Content-Type", "application/json"); // We send our data in JSON format
//        connection.setRequestProperty("User-Agent", "MC-Server/" + B_STATS_VERSION);
//
//        // Send data
//        connection.setDoOutput(true);
//        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream()))
//        {
//            outputStream.write(compressedData);
//        }
//
//        connection.getInputStream().close(); // We don't care about the response - Just send our data :)
//    }
//
//    /**
//     * Gzips the given String.
//     *
//     * @param str The string to gzip.
//     * @return The gzipped String.
//     * @throws IOException If the compression failed.
//     */
//    private static byte[] compress(final String str) throws IOException
//    {
//        if (str == null)
//        {
//            return null;
//        }
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream))
//        {
//            gzip.write(str.getBytes(StandardCharsets.UTF_8));
//        }
//        return outputStream.toByteArray();
//    }
//
//    /**
//     * Represents a custom chart.
//     */
//    public static abstract class CustomChart
//    {
//        // The id of the chart
//        private final String chartId;
//
//        /**
//         * Class constructor.
//         *
//         * @param chartId The id of the chart.
//         */
//        CustomChart(String chartId) {
//            if (chartId == null || chartId.isEmpty()) {
//                throw new IllegalArgumentException("ChartId cannot be null or empty!");
//            }
//            this.chartId = chartId;
//        }
//
//        private JsonObject getRequestJsonObject(Logger logger, boolean logFailedRequests)
//        {
//            JsonObject chart = new JsonObject();
//            chart.addProperty("chartId", chartId);
//            try {
//                JsonObject data = getChartData();
//                if (data == null) {
//                    // If the data is null we don't send the chart.
//                    return null;
//                }
//                chart.add("data", data);
//            } catch (Throwable t) {
//                if (logFailedRequests) {
//                    logger.warn("Failed to get data for custom chart with id {}", chartId, t);
//                }
//                return null;
//            }
//            return chart;
//        }
//
//        protected abstract JsonObject getChartData() throws Exception;
//    }
//
//    /**
//     * Represents a custom simple pie.
//     */
//    public static class SimplePie extends CustomChart {
//
//        private final Callable<String> callable;
//
//        /**
//         * Class constructor.
//         *
//         * @param chartId The id of the chart.
//         * @param callable The callable which is used to request the chart data.
//         */
//        public SimplePie(String chartId, Callable<String> callable) {
//            super(chartId);
//            this.callable = callable;
//        }
//
//        @Override
//        protected JsonObject getChartData() throws Exception {
//            JsonObject data = new JsonObject();
//            String value = callable.call();
//            if (value == null || value.isEmpty()) {
//                // Null = skip the chart
//                return null;
//            }
//            data.addProperty("value", value);
//            return data;
//        }
//    }
//
//    /**
//     * Represents a custom advanced pie.
//     */
//    public static class AdvancedPie extends CustomChart {
//
//        private final Callable<Map<String, Integer>> callable;
//
//        /**
//         * Class constructor.
//         *
//         * @param chartId The id of the chart.
//         * @param callable The callable which is used to request the chart data.
//         */
//        public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
//            super(chartId);
//            this.callable = callable;
//        }
//
//        @Override
//        protected JsonObject getChartData() throws Exception {
//            JsonObject data = new JsonObject();
//            JsonObject values = new JsonObject();
//            Map<String, Integer> map = callable.call();
//            if (map == null || map.isEmpty()) {
//                // Null = skip the chart
//                return null;
//            }
//            boolean allSkipped = true;
//            for (Map.Entry<String, Integer> entry : map.entrySet()) {
//                if (entry.getValue() == 0) {
//                    continue; // Skip this invalid
//                }
//                allSkipped = false;
//                values.addProperty(entry.getKey(), entry.getValue());
//            }
//            if (allSkipped) {
//                // Null = skip the chart
//                return null;
//            }
//            data.add("values", values);
//            return data;
//        }
//    }
//
//    /**
//     * Represents a custom drilldown pie.
//     */
//    public static class DrilldownPie extends CustomChart {
//
//        private final Callable<Map<String, Map<String, Integer>>> callable;
//
//        /**
//         * Class constructor.
//         *
//         * @param chartId The id of the chart.
//         * @param callable The callable which is used to request the chart data.
//         */
//        public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
//            super(chartId);
//            this.callable = callable;
//        }
//
//        @Override
//        public JsonObject getChartData() throws Exception {
//            JsonObject data = new JsonObject();
//            JsonObject values = new JsonObject();
//            Map<String, Map<String, Integer>> map = callable.call();
//            if (map == null || map.isEmpty()) {
//                // Null = skip the chart
//                return null;
//            }
//            boolean reallyAllSkipped = true;
//            for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
//                JsonObject value = new JsonObject();
//                boolean allSkipped = true;
//                for (Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
//                    value.addProperty(valueEntry.getKey(), valueEntry.getValue());
//                    allSkipped = false;
//                }
//                if (!allSkipped) {
//                    reallyAllSkipped = false;
//                    values.add(entryValues.getKey(), value);
//                }
//            }
//            if (reallyAllSkipped) {
//                // Null = skip the chart
//                return null;
//            }
//            data.add("values", values);
//            return data;
//        }
//    }
//
//    /**
//     * Represents a custom single line chart.
//     */
//    public static class SingleLineChart extends CustomChart {
//
//        private final Callable<Integer> callable;
//
//        /**
//         * Class constructor.
//         *
//         * @param chartId The id of the chart.
//         * @param callable The callable which is used to request the chart data.
//         */
//        public SingleLineChart(String chartId, Callable<Integer> callable) {
//            super(chartId);
//            this.callable = callable;
//        }
//
//        @Override
//        protected JsonObject getChartData() throws Exception {
//            JsonObject data = new JsonObject();
//            int value = callable.call();
//            if (value == 0) {
//                // Null = skip the chart
//                return null;
//            }
//            data.addProperty("value", value);
//            return data;
//        }
//
//    }
//
//    /**
//     * Represents a custom multi line chart.
//     */
//    public static class MultiLineChart extends CustomChart {
//
//        private final Callable<Map<String, Integer>> callable;
//
//        /**
//         * Class constructor.
//         *
//         * @param chartId The id of the chart.
//         * @param callable The callable which is used to request the chart data.
//         */
//        public MultiLineChart(String chartId, Callable<Map<String, Integer>> callable) {
//            super(chartId);
//            this.callable = callable;
//        }
//
//        @Override
//        protected JsonObject getChartData() throws Exception {
//            JsonObject data = new JsonObject();
//            JsonObject values = new JsonObject();
//            Map<String, Integer> map = callable.call();
//            if (map == null || map.isEmpty()) {
//                // Null = skip the chart
//                return null;
//            }
//            boolean allSkipped = true;
//            for (Map.Entry<String, Integer> entry : map.entrySet()) {
//                if (entry.getValue() == 0) {
//                    continue; // Skip this invalid
//                }
//                allSkipped = false;
//                values.addProperty(entry.getKey(), entry.getValue());
//            }
//            if (allSkipped) {
//                // Null = skip the chart
//                return null;
//            }
//            data.add("values", values);
//            return data;
//        }
//
//    }
//
//    /**
//     * Represents a custom simple bar chart.
//     */
//    public static class SimpleBarChart extends CustomChart {
//
//        private final Callable<Map<String, Integer>> callable;
//
//        /**
//         * Class constructor.
//         *
//         * @param chartId The id of the chart.
//         * @param callable The callable which is used to request the chart data.
//         */
//        public SimpleBarChart(String chartId, Callable<Map<String, Integer>> callable) {
//            super(chartId);
//            this.callable = callable;
//        }
//
//        @Override
//        protected JsonObject getChartData() throws Exception {
//            JsonObject data = new JsonObject();
//            JsonObject values = new JsonObject();
//            Map<String, Integer> map = callable.call();
//            if (map == null || map.isEmpty()) {
//                // Null = skip the chart
//                return null;
//            }
//            for (Map.Entry<String, Integer> entry : map.entrySet()) {
//                JsonArray categoryValues = new JsonArray();
//                categoryValues.add(new JsonPrimitive(entry.getValue()));
//                values.add(entry.getKey(), categoryValues);
//            }
//            data.add("values", values);
//            return data;
//        }
//
//    }
//
//    /**
//     * Represents a custom advanced bar chart.
//     */
//    public static class AdvancedBarChart extends CustomChart {
//
//        private final Callable<Map<String, int[]>> callable;
//
//        /**
//         * Class constructor.
//         *
//         * @param chartId The id of the chart.
//         * @param callable The callable which is used to request the chart data.
//         */
//        public AdvancedBarChart(String chartId, Callable<Map<String, int[]>> callable) {
//            super(chartId);
//            this.callable = callable;
//        }
//
//        @Override
//        protected JsonObject getChartData() throws Exception {
//            JsonObject data = new JsonObject();
//            JsonObject values = new JsonObject();
//            Map<String, int[]> map = callable.call();
//            if (map == null || map.isEmpty()) {
//                // Null = skip the chart
//                return null;
//            }
//            boolean allSkipped = true;
//            for (Map.Entry<String, int[]> entry : map.entrySet()) {
//                if (entry.getValue().length == 0) {
//                    continue; // Skip this invalid
//                }
//                allSkipped = false;
//                JsonArray categoryValues = new JsonArray();
//                for (int categoryValue : entry.getValue()) {
//                    categoryValues.add(new JsonPrimitive(categoryValue));
//                }
//                values.add(entry.getKey(), categoryValues);
//            }
//            if (allSkipped) {
//                // Null = skip the chart
//                return null;
//            }
//            data.add("values", values);
//            return data;
//        }
//
//    }
//}