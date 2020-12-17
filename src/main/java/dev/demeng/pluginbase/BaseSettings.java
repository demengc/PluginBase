package dev.demeng.pluginbase;

public interface BaseSettings {

  default String prefix() {
    return "&8[&bMyPlugin&8] ";
  }
}
