package dev.fiki.forgehax.asm.utils;

import lombok.SneakyThrows;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.DefaultErrorHandler;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static dev.fiki.forgehax.asm.ASMCommon.getLogger;

public class EZ {
  Logger fmlLogger;
  LAppender appender;

  @SneakyThrows
  public EZ() {

    //getUrl().ifPresent(this::disableBlocker);
    getJarPath().ifPresent(ModDirTransformerDiscoverer.getExtraLocators()::add);
  }

  public static Optional<URL> getOurJar() {
    final URL url = EZ.class.getProtectionDomain().getCodeSource().getLocation();
    final Path p;
    try {
      p = Paths.get(url.toURI());
    } catch (URISyntaxException ex) {
      throw new RuntimeException(ex);
    }

    if (p.getFileName().toString().toLowerCase().endsWith(".jar")) {
      return Optional.of(url);
    } else {
      return Optional.empty();
    }
  }

  public static Optional<Path> getJarPath() {
    return getOurJar().map(url -> {
      try {
        return Paths.get(url.toURI());
      } catch (URISyntaxException ex) {
        throw new RuntimeException(ex);
      }
    });
  }

  @SneakyThrows
  @Deprecated
  public void disableBlocker(URL url) {
    Class<?> transformerDiscoverer = Class.forName("net.minecraftforge.fml.loading.ModDirTransformerDiscoverer");

    Field transformersField = transformerDiscoverer.getDeclaredField("transformers");
    transformersField.setAccessible(true);

    transformersField.set(null, new OmittingTransformerList((ArrayList<Path>) transformersField.get(null), url.getFile()));
  }

  class LAppender implements Appender {
    final ErrorHandler defaultHandler = new DefaultErrorHandler(this);

    @Override
    public void append(LogEvent event) {
      if (event.getMessage() != null
          && event.getMessage().getFormat() != null
          && event.getMessage().getFormat().contains("Initiating mod scan")) {
        EZ.getOurJar().ifPresent(EZ.this::disableBlocker);
        fmlLogger.removeAppender(appender);
      }
    }

    @Override
    public String getName() {
      return "ForgeHaxListener";
    }

    @Override
    public Layout<? extends Serializable> getLayout() {
      return PatternLayout.createDefaultLayout();
    }

    @Override
    public boolean ignoreExceptions() {
      return false;
    }

    @Override
    public ErrorHandler getHandler() {
      return defaultHandler;
    }

    @Override
    public void setHandler(ErrorHandler handler) {
    }

    @Override
    public State getState() {
      return State.INITIALIZED;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
      return true;
    }

    @Override
    public boolean isStopped() {
      return false;
    }
  }

  static class OmittingTransformerList implements List<Path> {
    private final ArrayList<Path> original;
    private final String excluding;

    OmittingTransformerList(ArrayList<Path> original, String excluding) {
      this.original = original;

      if(System.getProperty("os.name").toLowerCase().contains("win")) {
        // omit the appending / from the file path if the user is on wangblows
        excluding = excluding.substring(1);
      }

      this.excluding = Paths.get(excluding).getFileName().toString();
      getLogger().info("Excluding file \"{}\" from transformer list", this.excluding);

      if(original.removeIf(path -> path.getFileName().toString().equals(this.excluding))) {
        getLogger().info("Existing transformer removed");
      }
    }

    @Override
    public int size() {
      return original.size();
    }

    @Override
    public boolean isEmpty() {
      return original.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      return original.contains(o);
    }

    @Override
    public Iterator<Path> iterator() {
      return original.iterator();
    }

    @Override
    public Object[] toArray() {
      return original.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
      return original.toArray(a);
    }

    @Override
    public boolean add(Path path) {
      if(path.getFileName().toString().equals(excluding)) {
        getLogger().info("Excluded transformer \"{}\"", path);
        return true;
      }
      getLogger().info("Included transformer \"{}\"", path);
      return original.add(path);
    }

    @Override
    public boolean remove(Object o) {
      return original.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return original.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Path> c) {
      return original.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Path> c) {
      return original.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      return original.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      return original.retainAll(c);
    }

    @Override
    public void clear() {
      original.clear();
    }

    @Override
    public Path get(int index) {
      return original.get(index);
    }

    @Override
    public Path set(int index, Path element) {
      return original.set(index, element);
    }

    @Override
    public void add(int index, Path element) {
      original.add(index, element);
    }

    @Override
    public Path remove(int index) {
      return original.remove(index);
    }

    @Override
    public int indexOf(Object o) {
      return original.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
      return original.lastIndexOf(o);
    }

    @Override
    public ListIterator<Path> listIterator() {
      return original.listIterator();
    }

    @Override
    public ListIterator<Path> listIterator(int index) {
      return original.listIterator(index);
    }

    @Override
    public List<Path> subList(int fromIndex, int toIndex) {
      return original.subList(fromIndex, toIndex);
    }

    @Override
    public void replaceAll(UnaryOperator<Path> operator) {
      original.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super Path> c) {
      original.sort(c);
    }

    @Override
    public Spliterator<Path> spliterator() {
      return original.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super Path> filter) {
      return original.removeIf(filter);
    }

    @Override
    public Stream<Path> stream() {
      return original.stream();
    }

    @Override
    public Stream<Path> parallelStream() {
      return original.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super Path> action) {
      original.forEach(action);
    }
  }
}
