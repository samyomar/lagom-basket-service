package com.elmenus.basket.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.elmenus.basket.api.BasketService;

/**
 * The module that binds the BasketService so that it can be served.
 */
public class BasketModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(BasketService.class, BasketServiceImpl.class);
  }
}
