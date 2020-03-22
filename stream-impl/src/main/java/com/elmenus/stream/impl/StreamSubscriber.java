package com.elmenus.stream.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import com.elmenus.basket.api.BasketEvent;
import com.elmenus.basket.api.BasketService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * This subscribes to the BasketService event stream.
 */
public class StreamSubscriber {
/*
  @Inject
  public StreamSubscriber(BasketService basketService, StreamRepository repository) {
    // Create a subscriber
    basketService.helloEvents().subscribe()
      // And subscribe to it with at least once processing semantics.
      .atLeastOnce(
        // Create a flow that emits a Done for each message it processes
        Flow.<BasketEvent>create().mapAsync(1, event -> {

          if (event instanceof BasketEvent.GreetingMessageChanged) {
            BasketEvent.GreetingMessageChanged messageChanged = (BasketEvent.GreetingMessageChanged) event;
            // Update the message
            return repository.updateMessage(messageChanged.getName(), messageChanged.getMessage());

          } else {
            // Ignore all other events
            return CompletableFuture.completedFuture(Done.getInstance());
          }
        })
      );

  }
  */
  
}
