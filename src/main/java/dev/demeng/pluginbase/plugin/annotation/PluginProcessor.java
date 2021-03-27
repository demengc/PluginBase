/*
 * MIT License
 *
 * Copyright (c) 2021 Demeng Chen
 * Copyright (c) lucko (Luck) <luck@lucko.me>
 * Copyright (c) github.com/lucko/helper contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.demeng.pluginbase.plugin.annotation;

import org.bukkit.plugin.PluginLoadOrder;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

/** Processes the {@link Plugin} annotation and generates a plugin.yml file at compile time. */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"dev.demeng.pluginbase.plugin.annotation.Plugin"})
public final class PluginProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

    final Set<? extends Element> annotatedElements = env.getElementsAnnotatedWith(Plugin.class);

    if (annotatedElements.isEmpty()) {
      return false;
    }

    if (annotatedElements.size() > 1) {
      this.processingEnv
          .getMessager()
          .printMessage(Diagnostic.Kind.ERROR, "More than one @Plugin element found.");
      return false;
    }

    final Element element = annotatedElements.iterator().next();

    if (!(element instanceof TypeElement)) {
      this.processingEnv
          .getMessager()
          .printMessage(Diagnostic.Kind.ERROR, "@Plugin element is not instance of TypeElement");
      return false;
    }

    final TypeElement type = ((TypeElement) element);
    final Map<String, Object> data = new LinkedHashMap<>();
    final Plugin annotation = type.getAnnotation(Plugin.class);

    data.put("name", annotation.name());

    final String version = annotation.version();
    final String description = annotation.description();
    final PluginLoadOrder order = annotation.load();
    final String apiVersion = annotation.apiVersion();
    final String[] authors = annotation.authors();
    final String website = annotation.website();
    final List<String> depends = new ArrayList<>(Arrays.asList(annotation.depends()));
    final List<String> softdepends = new ArrayList<>(Arrays.asList(annotation.softdepends()));
    final List<String> loadbefore = new ArrayList<>(Arrays.asList(annotation.loadbefore()));

    if (!version.isEmpty()) {
      data.put("version", version);
    } else {
      data.put(
          "version",
          new SimpleDateFormat("MM-dd-yyyy-HH-mm").format(new Date(System.currentTimeMillis())));
    }

    data.put("main", type.getQualifiedName().toString());

    if (!description.isEmpty()) {
      data.put("description", description);
    }

    if (order != PluginLoadOrder.POSTWORLD) {
      data.put("load", order.name());
    }

    if (!apiVersion.isEmpty()) {
      data.put("api-version", apiVersion);
    }

    if (authors.length == 1) {
      data.put("author", authors[0]);
    } else if (authors.length > 1) {
      data.put("authors", new ArrayList<>(Arrays.asList(authors)));
    }

    if (!website.isEmpty()) {
      data.put("website", website);
    }

    if (!depends.isEmpty()) {
      data.put("depend", depends);
    }

    if (!softdepends.isEmpty()) {
      data.put("softdepend", softdepends);
    }

    if (!loadbefore.isEmpty()) {
      data.put("loadbefore", loadbefore);
    }

    try {
      final Yaml yaml = new Yaml();
      final FileObject resource =
          this.processingEnv
              .getFiler()
              .createResource(StandardLocation.CLASS_OUTPUT, "", "plugin.yml");

      try (Writer writer = resource.openWriter();
          BufferedWriter bw = new BufferedWriter(writer)) {
        yaml.dump(data, bw);
        bw.flush();
      }

      return true;

    } catch (IOException ex) {
      throw new RuntimeException("Failed to generate plugin.yml: " + ex.getMessage(), ex);
    }
  }
}
