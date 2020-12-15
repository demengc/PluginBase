package dev.demeng.pluginbase.libloader;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import dev.demeng.pluginbase.plugin.DemPlugin;
import org.bukkit.Bukkit;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNull;

/**
 * Represents a runtime-downloaded plugin library.
 *
 * <p>Credit: https://github.com/ReflxctionDev/PluginLib
 */
public class PluginLib {

  public final String groupId, artifactId, version, repository;
  public final ImmutableSet<Relocation> relocationRules;
  private final boolean hasRelocations;

  /**
   * Creates a new plugin library to download at runtime.
   *
   * <p>Note: It is recommended that you use the {@link #builder()} instead.
   *
   * @param groupId The group ID of the library
   * @param artifactId The artifact ID of the library
   * @param version The version of the library
   * @param repository The repository of the library
   * @param relocationRules Relocation rules to apply to the library
   */
  public PluginLib(
      String groupId,
      String artifactId,
      String version,
      String repository,
      ImmutableSet<Relocation> relocationRules) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.repository = repository;
    this.relocationRules = relocationRules;
    hasRelocations = !relocationRules.isEmpty();
  }

  /**
   * Creates a new default builder,
   *
   * @return Creates a new default builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Create a new {@link Builder} from Maven-like XML code.
   *
   * @param xml XML to parse, must be exactly in maven format
   * @return A new {@link Builder} instance, derived from the XML
   * @throws IllegalArgumentException If the specified XML cannot be parsed
   */
  public static Builder parseXML(String xml) {

    try {
      final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      final InputSource is = new InputSource(new StringReader(xml));
      final Document doc = builder.parse(is);

      doc.getDocumentElement().normalize();

      return builder()
          .groupId(doc.getElementsByTagName("groupId").item(0).getTextContent())
          .artifactId(doc.getElementsByTagName("artifactId").item(0).getTextContent())
          .version(doc.getElementsByTagName("version").item(0).getTextContent());

    } catch (ParserConfigurationException | SAXException | IOException ex) {
      throw new IllegalArgumentException("Failed to parse XML: " + ex.getMessage());
    }
  }

  /**
   * Loads this library and handles any relocations if any.
   *
   * @param clazz Class to use its {@link ClassLoader} to load
   */
  public void load(Class<? extends DemPlugin> clazz) {

    final String name = artifactId + "-" + version;
    final File parent = libFile.get();
    File saveLocation = new File(parent, name + ".jar");

    if (!saveLocation.exists()) {
      try {
        URL url = asURL();
        saveLocation.createNewFile();

        try (InputStream is = url.openStream()) {
          Files.copy(is, saveLocation.toPath(), REPLACE_EXISTING);
        }

      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    if (!saveLocation.exists()) {
      throw new RuntimeException("Unable to download dependency: " + artifactId);
    }

    if (hasRelocations) {
      final File relocated = new File(parent, name + "-relocated.jar");

      if (!relocated.exists()) {
        try {
          relocated.createNewFile();
          FileRelocator.remap(
              saveLocation, new File(parent, name + "-relocated.jar"), relocationRules);

        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      saveLocation = relocated;
    }

    try {
      final URLClassLoader classLoader = (URLClassLoader) clazz.getClassLoader();
      addURL.invoke(classLoader, saveLocation.toURI().toURL());

    } catch (Exception ex) {
      throw new RuntimeException("Unable to load dependency: " + saveLocation.toString(), ex);
    }
  }

  /**
   * Creates a download {@link URL} for this library.
   *
   * @return The dependency URL
   * @throws MalformedURLException If the URL is malformed
   */
  public URL asURL() throws MalformedURLException {

    String repo = repository;

    if (!repo.endsWith("/")) {
      repo += "/";
    }

    repo += "%s/%s/%s/%s-%s.jar";

    return new URL(
        String.format(repo, groupId.replace(".", "/"), artifactId, version, artifactId, version));
  }

  @Override
  public String toString() {
    return "PluginLib{"
        + "groupId='"
        + groupId
        + '\''
        + ", artifactId='"
        + artifactId
        + '\''
        + ", version='"
        + version
        + '\''
        + ", repository='"
        + repository
        + '\''
        + ", relocationRules="
        + relocationRules
        + ", hasRelocations="
        + hasRelocations
        + '}';
  }

  private static Method addURL;

  static {
    try {
      addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      addURL.setAccessible(true);
    } catch (Throwable ex) {
      ex.printStackTrace();
    }
  }

  public static class Builder {

    private String url = null;
    private String group;
    private String artifact;
    private String version;
    private String repository = "https://repo1.maven.org/maven2/";
    private final ImmutableSet.Builder<Relocation> relocations = ImmutableSet.builder();

    protected Builder() {}

    /**
     * Sets the builder to create a static URL dependency.
     *
     * @param url URL of the dependency
     * @return This builder
     */
    public Builder fromURL(String url) {
      this.url = n(url, "URL is null");
      return this;
    }

    /**
     * Sets the group ID of the dependency.
     *
     * @param group New group ID to set
     * @return This builder
     */
    public Builder groupId(String group) {
      this.group = n(group, "groupId is null");
      return this;
    }

    /**
     * Sets the artifact ID of the dependency.
     *
     * @param artifact New artifact ID to set
     * @return This builder
     */
    public Builder artifactId(String artifact) {
      this.artifact = n(artifact, "artifactId is null");
      return this;
    }

    /**
     * Sets the version of the dependency.
     *
     * @param version New version to set
     * @return This builder
     */
    public Builder version(String version) {
      this.version = n(version, "version is null");
      return this;
    }

    /**
     * Sets the repository to download the dependency from.
     *
     * @param repository New repository to set
     * @return This builder
     */
    public Builder repository(String repository) {
      this.repository = requireNonNull(repository);
      return this;
    }

    /**
     * A convenience method to set the repository to <em>JitPack</em>.
     *
     * @return This builder
     */
    public Builder jitpack() {
      return repository("https://jitpack.io/");
    }

    /**
     * A convenience method to set the repository to <em>Bintray - JCenter</em>.
     *
     * @return This builder
     */
    public Builder jcenter() {
      return repository("https://jcenter.bintray.com/");
    }

    /**
     * A convenience method to set the repository to <em>Maven Central</em>.
     *
     * @return This builder
     */
    public Builder mavenCentral() {
      return repository("https://repo1.maven.org/maven2/");
    }

    /**
     * A convenience method to set the repository to <em>Aikar's Repository</em>.
     *
     * @return This builder
     */
    public Builder aikar() {
      return repository("https://repo.aikar.co/content/groups/aikar/");
    }

    /**
     * Adds a new relocation rule.
     *
     * @param relocation New relocation rule to add
     * @return This builder
     */
    public Builder relocate(Relocation relocation) {
      relocations.add(n(relocation, "Relocation is null"));
      return this;
    }

    /**
     * Constructs a {@link PluginLib} from the provided values.
     *
     * @return A new, immutable {@link PluginLib} instance
     * @throws NullPointerException If any of the required properties is not provided
     */
    public PluginLib build() {

      if (url != null) {
        return new StaticURLPluginLib(
            group,
            n(artifact, "artifactId"),
            n(version, "version"),
            repository,
            relocations.build(),
            url);
      }

      return new PluginLib(
          n(group, "groupId"),
          n(artifact, "artifactId"),
          n(version, "version"),
          n(repository, "repository"),
          relocations.build());
    }

    private static <T> T n(T t, String m) {
      return requireNonNull(t, m);
    }
  }

  /**
   * A convenience method to check whether a class exists at runtime or not.
   *
   * @param className Class name to check for
   * @return true if the class exists, false if otherwise.
   */
  public static boolean classExists(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }

  private static final List<PluginLib> toInstall = new ArrayList<>();

  public static void loadLibs() {
    for (PluginLib pluginLib : toInstall) {
      pluginLib.load(DemPlugin.class);
    }
  }

  private static class LibrariesOptions {

    private String relocationPrefix = null;
    private String librariesFolder = "libs";
    private Map<String, String> globalRelocations = Collections.emptyMap();
    private Map<String, RuntimeLib> libraries = Collections.emptyMap();

    public static LibrariesOptions fromMap(Map<String, Object> map) {

      final LibrariesOptions options = new LibrariesOptions();
      options.relocationPrefix = (String) map.get("relocation-prefix");
      options.librariesFolder = (String) map.getOrDefault("libraries-folder", "libs");
      options.globalRelocations =
          (Map<String, String>) map.getOrDefault("global-relocations", Collections.emptyMap());
      options.libraries = new HashMap<>();
      Map<String, Map<String, Object>> declaredLibs =
          (Map<String, Map<String, Object>>) map.get("libraries");

      if (declaredLibs != null) {
        for (Entry<String, Map<String, Object>> lib : declaredLibs.entrySet()) {
          options.libraries.put(lib.getKey(), RuntimeLib.fromMap(lib.getValue()));
        }
      }

      return options;
    }
  }

  private static class RuntimeLib {

    private String xml = null;

    private String url = null;
    private String groupId = null, artifactId = null, version = null;
    private Map<String, String> relocation = null;
    private String repository = null;

    Builder builder() {
      Builder b;
      if (url != null) b = PluginLib.builder().fromURL(url);
      else if (xml != null) b = parseXML(xml);
      else b = new Builder();
      if (groupId != null) b.groupId(groupId);
      if (artifactId != null) b.artifactId(artifactId);
      if (version != null) b.version(version);
      if (repository != null) b.repository(repository);
      return b;
    }

    static RuntimeLib fromMap(Map<String, Object> map) {
      RuntimeLib lib = new RuntimeLib();
      lib.xml = (String) map.get("xml");
      lib.url = (String) map.get("url");
      lib.groupId = (String) map.get("groupId");
      lib.artifactId = (String) map.get("artifactId");
      lib.version = (String) map.get("version");
      lib.repository = (String) map.get("repository");
      lib.relocation = (Map<String, String>) map.get("relocation");
      return lib;
    }
  }

  private static class StaticURLPluginLib extends PluginLib {

    private final String url;

    public StaticURLPluginLib(
        String groupId,
        String artifactId,
        String version,
        String repository,
        ImmutableSet<Relocation> relocationRules,
        String url) {
      super(groupId, artifactId, version, repository, relocationRules);
      this.url = url;
    }

    @Override
    public URL asURL() throws MalformedURLException {
      return new URL(url);
    }
  }

  private static final Supplier<File> libFile =
      Suppliers.memoize(
          () -> {
            final Map<?, ?> map =
                new Yaml()
                    .load(
                        new InputStreamReader(
                            requireNonNull(
                                DemPlugin.class.getClassLoader().getResourceAsStream("plugin.yml"),
                                "Jar does not contain plugin.yml")));

            final String name = map.get("name").toString();
            String folder = "libs";

            if (map.containsKey("runtime-libraries")) {
              LibrariesOptions options =
                  LibrariesOptions.fromMap(((Map<String, Object>) map.get("runtime-libraries")));

              if (options.librariesFolder != null && !options.librariesFolder.isEmpty()) {
                folder = options.librariesFolder;
              }

              final String prefix =
                  options.relocationPrefix == null ? null : options.relocationPrefix;
              requireNonNull(prefix, "Relocation prefix must be defined in runtime-libraries");

              final Set<Relocation> globalRelocations = new HashSet<>();
              for (Entry<String, String> global : options.globalRelocations.entrySet()) {
                globalRelocations.add(
                    new Relocation(global.getKey(), prefix + "." + global.getValue()));
              }

              for (Entry<String, RuntimeLib> lib : options.libraries.entrySet()) {

                final RuntimeLib runtimeLib = lib.getValue();
                final Builder b = runtimeLib.builder();

                if (runtimeLib.relocation != null && !runtimeLib.relocation.isEmpty()) {
                  for (Entry<String, String> s : runtimeLib.relocation.entrySet()) {
                    b.relocate(new Relocation(s.getKey(), prefix + "." + s.getValue()));
                  }
                }

                for (Relocation relocation : globalRelocations) {
                  b.relocate(relocation);
                }

                toInstall.add(b.build());
              }
            }

            final File file =
                new File(
                    Bukkit.getUpdateFolderFile().getParentFile() + File.separator + name, folder);
            file.mkdirs();
            return file;
          });
}
