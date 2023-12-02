package cl.brown.amelia.ui.device.placeholder;

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
public class OperateDeviceContent {

    /**
     * An array of sample (placeholder) items.
     */
    public static final List<OperateDeviceItem> ITEMS = new ArrayList<OperateDeviceItem>();

    /**
     * A map of sample (placeholder) items, by ID.
     */
    public static final Map<String, OperateDeviceItem> ITEM_MAP = new HashMap<String, OperateDeviceItem>();

    private static final int COUNT = 0;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createPlaceholderItem(i));
        }
    }

    private static void addItem(OperateDeviceItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.serial, item);
    }

    private static OperateDeviceItem createPlaceholderItem(int position) {
        return new OperateDeviceItem(String.valueOf(position), "Item " + position, makeDetails(position), "true");
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
    public static class OperateDeviceItem {
        public final String serial;
        public final String ip;
        public final String port;
        public final String config;

        public OperateDeviceItem(String serial, String ip, String port, String config) {
            this.serial = serial;
            this.ip = ip;
            this.port = port;
            this.config = config;
        }

        @Override
        public String toString() {
            return serial;
        }
    }
}