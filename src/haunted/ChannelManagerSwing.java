package haunted;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * ChannelManagerSwing
 * - Channels 1–4: random podcast with static glitches
 * - Channel 5: short static (riddle handled in UI)
 * - Channel 666: ghost broadcast + hidden Morse
 *
 * Looks for audio under:
 *   disk:     audio/...
 *   classpath: /audio/...
 * Folder name fixed to "spanish_podcast" (singular) to match your resources.
 */
public class ChannelManagerSwing {

    private final Path diskAudioRoot;
    private final AudioPlayer player = new AudioPlayer();
    private final Random rng = new Random();
    private final Map<Integer, List<Path>> channelPods = new HashMap<>();

    public ChannelManagerSwing(Path diskAudioRoot) {
        this.diskAudioRoot = diskAudioRoot;
        loadPodcasts();
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public boolean trySolveRiddle(String answer) {
        String a = (answer == null) ? "" : answer.trim().toLowerCase(Locale.ROOT);
        return a.contains("shadow") || a.contains("sombra");
    }

    public void playChannel(int ch) {
        if (ch >= 1 && ch <= 4) {
            List<Path> list = channelPods.getOrDefault(ch, List.of());
            if (list.isEmpty()) { playStatic(1200); return; }
            Path episode = list.get(rng.nextInt(list.size()));
            Path staticPath = resolveToPathOrTemp("audio/static.wav", "audio/static.wav");
            if (staticPath == null) {
                player.playWavForMillis(episode, 5000);
            } else {
                player.playWavWithOccasionalGlitch(episode, staticPath, 0.12);
            }
        } else if (ch == 5) {
            playStatic(600);
        }
    }// add the off switch

    public void playGhost() {
        Path ghost = resolveToPathOrTemp("audio/ghost_broadcast.wav", "audio/ghost_broadcast.wav");
        Path staticPath = resolveToPathOrTemp("audio/static.wav", "audio/static.wav");
        if (ghost != null && staticPath != null) {
            player.playWavWithOccasionalGlitch(ghost, staticPath, 0.18);
        } else {
            playStatic(1200);
        }
        MorseCode.playMessage("THIS IS DEFINITELY AN A");
    }

    public void playStatic(int ms) {
        Path staticPath = resolveToPathOrTemp("audio/static.wav", "audio/static.wav");
        if (staticPath != null) player.playWavForMillis(staticPath, ms);
    }

    // -------------------- internals --------------------

    /** Build episode lists for channels 1–4 from audio/spanish_podcast */
    private void loadPodcasts() {
        // 🔧 Folder fixed here (singular):
        List<Path> episodes = listWavsInFolder("audio/spanish_podcast", "audio/spanish_podcast");
        for (int ch = 1; ch <= 4; ch++) channelPods.put(ch, episodes);
    }

    /** Prefer disk; if missing, copy classpath resource to a temp file and return that Path. */
    private Path resolveToPathOrTemp(String diskRelative, String classpathResource) {
        try {
            Path p = (diskAudioRoot != null && diskRelative.startsWith("audio/"))
                    ? diskAudioRoot.resolve(diskRelative.substring("audio/".length()))
                    : Paths.get(diskRelative);
            if (Files.exists(p)) return p;
        } catch (Exception ignored) {}

        try {
            URL url = getClass().getClassLoader().getResource(classpathResource);
            if (url == null) return null;
            try (InputStream in = url.openStream()) {
                Path tmp = Files.createTempFile("hr_", "_" + Paths.get(classpathResource).getFileName());
                tmp.toFile().deleteOnExit();
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                return tmp;
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** List .wav files from disk folder or classpath folder (when resources are copied to out/...). */
    private List<Path> listWavsInFolder(String diskFolder, String classpathFolder) {
        List<Path> out = new ArrayList<>();

        // disk first
        try {
            Path dir = (diskAudioRoot != null && diskFolder.startsWith("audio/"))
                    ? diskAudioRoot.resolve(diskFolder.substring("audio/".length()))
                    : Paths.get(diskFolder);
            if (Files.isDirectory(dir)) {
                try (Stream<Path> s = Files.list(dir)) {
                    s.filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".wav")).forEach(out::add);
                }
                return out;
            }
        } catch (Exception ignored) {}

        // classpath (file protocol) — works when resources are copied to out/
        try {
            URL url = getClass().getClassLoader().getResource(classpathFolder);
            if (url != null && "file".equalsIgnoreCase(url.getProtocol())) {
                Path cpDir = Paths.get(url.toURI());
                if (Files.isDirectory(cpDir)) {
                    try (Stream<Path> s = Files.list(cpDir)) {
                        s.filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".wav")).forEach(out::add);
                    }
                }
            }
        } catch (Exception ignored) {}
        return out;
    }
}
