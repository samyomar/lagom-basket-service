package com.elmenus.stream.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.elmenus.basket.api.BasketService;
import com.elmenus.stream.api.StreamService;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Implementation of the HelloString.
 */
public class StreamServiceImpl implements StreamService {

  private final BasketService basketService;
  private final StreamRepository repository;

  @Inject
  public StreamServiceImpl(BasketService basketService, StreamRepository repository) {
    this.basketService = basketService;
    this.repository = repository;
  }

 @Override
  public ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> directStream() {
    return null ;
    /* return hellos -> completedFuture(
      hellos.mapAsync(8, name ->  basketService.hello(name).invoke()));*/
  }

  @Override
  public ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> autonomousStream() {
    return hellos -> completedFuture(
        hellos.mapAsync(8, name -> repository.getMessage(name).thenApply( message ->
            String.format("%s, %s!", message.orElse("Hello"), name)
        ))
    );
  }
}
