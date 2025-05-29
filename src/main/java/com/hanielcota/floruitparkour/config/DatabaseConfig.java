package com.hanielcota.floruitparkour.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DatabaseConfig {
  private final String host;
  private final int port;
  private final String databaseName;
  private final String user;
  private final String password;
}
