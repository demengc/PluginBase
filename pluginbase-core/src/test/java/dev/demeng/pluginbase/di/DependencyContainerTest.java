package dev.demeng.pluginbase.di;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.demeng.pluginbase.di.annotation.Component;
import dev.demeng.pluginbase.di.container.DependencyContainer;
import dev.demeng.pluginbase.di.exception.DependencyException;
import dev.demeng.pluginbase.di.exception.MissingDependencyException;
import dev.demeng.pluginbase.di.exception.TypeMismatchException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class DependencyContainerTest {

  interface Greeter {
    String greet();
  }

  static class EnglishGreeter implements Greeter {
    @Override
    public String greet() {
      return "hello";
    }
  }

  @Component
  static class NoArgService {
    String name() {
      return "no-arg";
    }
  }

  @Component
  static class ServiceWithDeps {
    private final NoArgService dependency;

    ServiceWithDeps(NoArgService dependency) {
      this.dependency = dependency;
    }
  }

  @Component
  static class CircularA {
    CircularA(CircularB b) {}
  }

  @Component
  static class CircularB {
    CircularB(CircularA a) {}
  }

  @Component
  static class MultiConstructor {
    private final String value;

    MultiConstructor() {
      this.value = "default";
    }

    MultiConstructor(NoArgService service) {
      this.value = "injected";
    }
  }

  static class CloseableService implements AutoCloseable {
    final AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public void close() {
      closed.set(true);
    }
  }

  static class UnregisteredPlain {}

  @Test
  void register_andRetrieve_byConcreteType() {
    DependencyContainer container =
        DependencyContainer.builder().register(new EnglishGreeter()).build();
    assertThat(container.getInstance(EnglishGreeter.class).greet()).isEqualTo("hello");
  }

  @Test
  void register_andRetrieve_byInterfaceType() {
    Greeter greeter = new EnglishGreeter();
    DependencyContainer container =
        DependencyContainer.builder().register(Greeter.class, greeter).build();
    assertThat(container.getInstance(Greeter.class)).isSameAs(greeter);
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void register_incompatibleType_throwsTypeMismatchException() {
    DependencyContainer container = DependencyContainer.builder().build();
    Class rawType = Greeter.class;
    assertThatThrownBy(() -> container.register(rawType, "not a greeter"))
        .isInstanceOf(TypeMismatchException.class);
  }

  @Test
  void getInstance_component_noArgConstructor_autoCreates() {
    DependencyContainer container = DependencyContainer.builder().build();
    NoArgService service = container.getInstance(NoArgService.class);
    assertThat(service).isNotNull();
    assertThat(service.name()).isEqualTo("no-arg");
  }

  @Test
  void getInstance_component_withDependencies_injectsThem() {
    DependencyContainer container = DependencyContainer.builder().build();
    ServiceWithDeps service = container.getInstance(ServiceWithDeps.class);
    assertThat(service.dependency).isNotNull();
    assertThat(service.dependency.name()).isEqualTo("no-arg");
  }

  @Test
  void getInstance_circularDependency_throwsDependencyException() {
    DependencyContainer container = DependencyContainer.builder().build();
    assertThatThrownBy(() -> container.getInstance(CircularA.class))
        .isInstanceOf(DependencyException.class)
        .hasMessageContaining("Circular dependency detected");
  }

  @Test
  void getInstance_unregisteredNonComponent_throwsMissingDependencyException() {
    DependencyContainer container = DependencyContainer.builder().build();
    assertThatThrownBy(() -> container.getInstance(UnregisteredPlain.class))
        .isInstanceOf(MissingDependencyException.class);
  }

  @Test
  void getInstance_returnsSameInstance_singletonGuarantee() {
    DependencyContainer container = DependencyContainer.builder().build();
    NoArgService first = container.getInstance(NoArgService.class);
    NoArgService second = container.getInstance(NoArgService.class);
    assertThat(first).isSameAs(second);
  }

  @Test
  void register_duplicate_overwritesPrevious() {
    EnglishGreeter first = new EnglishGreeter();
    EnglishGreeter second = new EnglishGreeter();
    DependencyContainer container = DependencyContainer.builder().register(first).build();
    container.register(second);
    assertThat(container.getInstance(EnglishGreeter.class)).isSameAs(second);
  }

  @Test
  void hasInstance_registeredType_returnsTrue() {
    DependencyContainer container =
        DependencyContainer.builder().register(new EnglishGreeter()).build();
    assertThat(container.hasInstance(EnglishGreeter.class)).isTrue();
  }

  @Test
  void hasInstance_componentType_returnsTrue() {
    DependencyContainer container = DependencyContainer.builder().build();
    assertThat(container.hasInstance(NoArgService.class)).isTrue();
  }

  @Test
  void hasInstance_unknownType_returnsFalse() {
    DependencyContainer container = DependencyContainer.builder().build();
    assertThat(container.hasInstance(UnregisteredPlain.class)).isFalse();
  }

  @Test
  void close_preventsSubsequentGetInstance() throws Exception {
    DependencyContainer container = DependencyContainer.builder().build();
    container.close();
    assertThatThrownBy(() -> container.getInstance(NoArgService.class))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void close_isIdempotent() throws Exception {
    DependencyContainer container = DependencyContainer.builder().build();
    container.close();
    container.close();
    assertThat(container.isClosed()).isTrue();
  }

  @Test
  void close_cleansUpAutoCloseableInstances() throws Exception {
    CloseableService service = new CloseableService();
    DependencyContainer container = DependencyContainer.builder().register(service).build();
    container.close();
    assertThat(service.closed.get()).isTrue();
  }

  @Test
  void getInstance_multipleConstructors_selectsLongest() {
    DependencyContainer container = DependencyContainer.builder().build();
    MultiConstructor instance = container.getInstance(MultiConstructor.class);
    assertThat(instance.value).isEqualTo("injected");
  }
}
