package vn.iotstar.util;

import java.util.Map;

public final class SongPlaylistMetadata {
    private static final Map<String, String> YOUTUBE_URLS = Map.ofEntries(
            Map.entry("Khóc Đấy (Album Version)", "https://www.youtube.com/watch?v=poGyHfrJ_uo&list=PLniuYvnK-UyXyzsUE-DpZRD7R9gnHHVlI&index=1"),
            Map.entry("Bút Chì Bạc (Album Version)", "https://www.youtube.com/watch?v=aNTytiDilwo&list=PLniuYvnK-UyXyzsUE-DpZRD7R9gnHHVlI&index=2"),
            Map.entry("Hoá Ra Là (feat. Wala)", "https://www.youtube.com/watch?v=yfMNflXnfBc&list=PLniuYvnK-UyXyzsUE-DpZRD7R9gnHHVlI&index=3"),
            Map.entry("Gội Đầu (feat. Hà Lê)", "https://www.youtube.com/watch?v=HUUPeCZKw24&list=PLniuYvnK-UyXyzsUE-DpZRD7R9gnHHVlI&index=4"),
            Map.entry("100%", "https://www.youtube.com/watch?v=Ze-FKjZ1v1U&list=PLniuYvnK-UyXyzsUE-DpZRD7R9gnHHVlI&index=5"),
            Map.entry("Căn Gác Lặng", "https://www.youtube.com/watch?v=W_2GR6FlSSw&list=PLniuYvnK-UyXyzsUE-DpZRD7R9gnHHVlI&index=6"),
            Map.entry("Đồng Ý (kết hợp với Thơ Tơ Mơ)", "https://www.youtube.com/watch?v=EpX20Q8miwE&list=PLniuYvnK-UyXyzsUE-DpZRD7R9gnHHVlI&index=7"),
            Map.entry("60m Vuông", "https://www.youtube.com/watch?v=zP7Yappevro&list=PLniuYvnK-UyXyzsUE-DpZRD7R9gnHHVlI&index=8"),
            Map.entry("Nấu Con Beat (feat. Wala)", "https://www.youtube.com/watch?v=C5jA8uA-9Ps&list=PLniuYvnK-UyXyzsUE-DpZRD7R9gnHHVlI&index=9"),
            Map.entry("Rất (feat. SUNI, Pixel Neko)", "https://www.youtube.com/watch?v=EHMfs1YYXi4&list=PLniuYvnK-UyXyzsUE-DpZRD7R9gnHHVlI&index=10"),
            Map.entry("Sáng Ra Chỉ Cần", "https://www.youtube.com/watch?v=fzPcKYuwMLw&list=PLniuYvnK-UyXyzsUE-DpZRD7R9gnHHVlI&index=11"),
            Map.entry("Tình Nhân Muôn Kiếp", "https://www.youtube.com/watch?v=IEZeCM8H_7A&list=PLniuYvnK-UyXyzsUE-DpZRD7R9gnHHVlI&index=12")
    );

    private SongPlaylistMetadata() {
    }

    public static String youtubeUrlFor(String productName) {
        if (productName == null) {
            return null;
        }
        return YOUTUBE_URLS.get(productName);
    }
}
