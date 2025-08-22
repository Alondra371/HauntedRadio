package haunted;

import java.io.*;
import java.net.URL;
import java.nio.file.*;

public final class ResourceAudio {
    private ResourceAudio() {}

    /** Try disk path first (e.g., audio/static.wav). If not found, try classpath (resources/audio/static.wav). */
    public static Path resolveToPathOrTemp(String diskRelative, String classpathResource) {
        // 1) disk
        Path p = Paths.get(diskRelative);
        if (Files.exists(p)) return p;

        // 2) classpath -> copy to temp file so AudioPlayer (Path-based) can stream it
        try {
            URL url = ResourceAudio.class.getClassLoader().getResource(classpathResource);
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

    /** Returns the directory Path if the disk folder exists; otherwise returns a classpath URL for folder listing. */
    public static FolderHandle resolveFolder(String diskFolder, String classpathFolder) {
        Path disk = Paths.get(diskFolder);
        if (Files.isDirectory(disk)) return FolderHandle.disk(disk);
        URL url = ResourceAudio.class.getClassLoader().getResource(classpathFolder);
        return (url != null) ? FolderHandle.cp(url) : null;
    }

    /** Small helper to represent either a disk folder (Path) or a classpath folder (URL:file) */
    public static final class FolderHandle {
        public final Path diskPath; public final URL cpUrl;
        private FolderHandle(Path p, URL u){ this.diskPath=p; this.cpUrl=u; }
        public static FolderHandle disk(Path p){ return new FolderHandle(p, null); }
        public static FolderHandle cp(URL u){ return new FolderHandle(null, u); }
        public boolean isDisk(){ return diskPath != null; }
    }
}
