package cl.brown.amelia.ui.wifi.placeholder;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class WifiContent {

    /**
     * An array of sample (placeholder) items.
     */
    public static final List<WifiItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample (placeholder) items, by ID.
     */
    public static final Map<String, WifiItem> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 0;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createPlaceholderItem(i));
        }
    }

    private static void addItem(WifiItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.ssid, item);
    }

    private static WifiItem createPlaceholderItem(int position) {
        return new WifiItem(String.valueOf(position), position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A placeholder item representing a piece of content.
     */
    public static class WifiItem {
        public final String ssid;
        public final Integer waveLevel;
        public final String securityType;

        public WifiItem(String ssid, Integer waveLevel, String securityType) {
            this.ssid = ssid;
            this.waveLevel = waveLevel;
            this.securityType = securityType;
        }

        @NonNull
        @Override
        public String toString() {
            return String.valueOf(waveLevel);
        }
    }
}