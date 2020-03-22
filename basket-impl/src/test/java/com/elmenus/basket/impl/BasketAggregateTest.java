package com.elmenus.basket.impl;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import com.elmenus.basket.impl.BasketCommand.Hello;
import com.elmenus.basket.impl.BasketCommand.UseGreetingMessage;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.UUID;

public class BasketAggregateTest {
  private static final String inmemConfig =
      "akka.persistence.journal.plugin = \"akka.persistence.journal.inmem\" \n";

  private static final String snapshotConfig =
      "akka.persistence.snapshot-store.plugin = \"akka.persistence.snapshot-store.local\" \n"
      + "akka.persistence.snapshot-store.local.dir = \"target/snapshot-"
      + UUID.randomUUID().toString()
      + "\" \n";

  private static final String config = inmemConfig + snapshotConfig;

  @ClassRule
  public static final TestKitJunitResource testKit = new TestKitJunitResource(config);

  @Test
  public void testHello() {

      String id = "Alice";
      ActorRef<BasketCommand> ref =
        testKit.spawn(
          BasketAggregate.create(
            // Unit testing the Aggregate requires an EntityContext but starting
            // a complete Akka Cluster or sharding the actors is not requried.
            // The actorRef to the shard can be null as it won't be used.
            new EntityContext(BasketAggregate.ENTITY_TYPE_KEY, id,  null)
          )
        );

      TestProbe<BasketCommand.Greeting> probe =
        testKit.createTestProbe(BasketCommand.Greeting.class);
      ref.tell(new Hello(id,probe.getRef()));
      probe.expectMessage(new BasketCommand.Greeting("Hello, Alice!"));
  }

  @Test
  public void testUpdateGreeting() {
      String id = "Alice";
      ActorRef<BasketCommand> ref =
        testKit.spawn(
          BasketAggregate.create(
            // Unit testing the Aggregate requires an EntityContext but starting
            // a complete Akka Cluster or sharding the actors is not requried.
            // The actorRef to the shard can be null as it won't be used.
           new EntityContext(BasketAggregate.ENTITY_TYPE_KEY, id,  null)
          )
        );

      TestProbe<BasketCommand.Confirmation> probe1 =
        testKit.createTestProbe(BasketCommand.Confirmation.class);
      ref.tell(new UseGreetingMessage("Hi", probe1.getRef()));
      probe1.expectMessage(new BasketCommand.Accepted());

      TestProbe<BasketCommand.Greeting> probe2 =
        testKit.createTestProbe(BasketCommand.Greeting.class);
      ref.tell(new Hello(id,probe2.getRef()));
      probe2.expectMessage(new BasketCommand.Greeting("Hi, Alice!"));
    }
}
