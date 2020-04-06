package com.elmenus.basket.impl;

import akka.Done;
import com.elmenus.basket.api.BasketItem;
import com.elmenus.basket.api.BasketService;
import com.elmenus.basket.api.BasketView;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

public class BasketServiceTesting {

    private static TestServer server;

    @BeforeClass
    public static void setUp() {
        server = startServer(defaultSetup().withCluster(false).withCassandra(true));
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void getEmptyBasket() throws Exception {
        BasketService service = server.client(BasketService.class);
        BasketView basketView = service.getBasket(UUID.randomUUID()).invoke().toCompletableFuture().get(3, SECONDS);
        assertTrue("items list should be empty" , basketView.getItems()!=null &&  basketView.getItems().isEmpty());
    }

    @Test
    public void checkAddItem() throws Exception {
        BasketService service = server.client(BasketService.class);
        BasketItem item =  new BasketItem("c9f3c98b-e680-4090-bfac-c60aca3d1db7","5","8");
        Done done = service
                    .addItem(UUID.fromString("356771b2-301a-40ab-8bf5-17fed0d7c275"))
                    .handleRequestHeader(rh -> rh.withHeader("UserUuid" ,"1927a910-5045-43d6-9242-19b8c01a96cc"))
                    .invoke(item).toCompletableFuture().get(3,SECONDS);
        System.out.println(done.toString());
        assertNotNull(done);
    }

    @Test
    public void checkBasketComplexScenario() throws Exception {

        // 1- creating an item with new basket UUID
        BasketView basketView;
        Done done;
        UUID basketUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();

        UUID item1_UUID = UUID.randomUUID();
        UUID item2_UUID = UUID.randomUUID();
        UUID item3_UUID = UUID.randomUUID();

        BasketService service = server.client(BasketService.class);
        BasketItem item1 =  new BasketItem(item1_UUID.toString(),"2","5");
        BasketItem item2 =  new BasketItem(item2_UUID.toString(),"3","10");
        BasketItem item3 =  new BasketItem(item3_UUID.toString(),"4","20");
        BasketItem item2_updated =  new BasketItem(item2_UUID.toString(),"5","15");

        // add the the first item to the basket.
        done = service
                .addItem(basketUUID)
                .handleRequestHeader(rh -> rh.withHeader("UserUuid" ,userUUID.toString()))
                .invoke(item1).toCompletableFuture().get(3,SECONDS);
        System.out.println(done.toString());
        assertNotNull(done);

        // check if the item exist by quering the get service
        basketView = server.client(BasketService.class).getBasket(basketUUID).invoke().toCompletableFuture().get(3, SECONDS);
        assertEquals("total is not correct","11",basketView.getTotal());

        // add the second item
        done = service
                .addItem(basketUUID)
                .handleRequestHeader(rh -> rh.withHeader("UserUuid" ,userUUID.toString()))
                .invoke(item2).toCompletableFuture().get(3,SECONDS);
        System.out.println(done.toString());
        assertNotNull(done);

        // check the basket info.
        basketView = server.client(BasketService.class).getBasket(basketUUID).invoke().toCompletableFuture().get(3, SECONDS);
        assertEquals("total is not correct","44",basketView.getTotal());

        // add the third item
        done = service
                .addItem(basketUUID)
                .handleRequestHeader(rh -> rh.withHeader("UserUuid" ,userUUID.toString()))
                .invoke(item3).toCompletableFuture().get(3,SECONDS);
        System.out.println(done.toString());
        assertNotNull(done);

        // check the basket calculations
        basketView = server.client(BasketService.class).getBasket(basketUUID).invoke().toCompletableFuture().get(3, SECONDS);
        assertEquals("total is not correct","132",basketView.getTotal());


        // add an already existing item:
        done = service
                .addItem(basketUUID)
                .handleRequestHeader(rh -> rh.withHeader("UserUuid" ,userUUID.toString()))
                .invoke(item2_updated).toCompletableFuture().get(3,SECONDS);
        System.out.println(done.toString());
        assertNotNull(done);

        // it should update the existing one only.
        basketView = server.client(BasketService.class).getBasket(basketUUID).invoke().toCompletableFuture().get(3, SECONDS);
        assertEquals("total is not correct","181.5",basketView.getTotal());

        // another user trying to add the the same basket.
        try {
            done = service
                    .addItem(basketUUID)
                    .handleRequestHeader(rh -> rh.withHeader("UserUuid", UUID.randomUUID().toString()))
                    .invoke(item2_updated).toCompletableFuture().get(3, SECONDS);
            assertFalse(done.toString().contains("done"));
        }
        catch(Exception e) // using the general exception for simplicity
        {
        }
    }




    /*
    @Test
    public void checkDuplicateItems() throws Exception {
        withServer(
                defaultSetup(),
                server -> {
                    BasketService service = server.client(BasketService.class);

                    BasketView basketView = service.getBasket(UUID.randomUUID()).invoke().toCompletableFuture().get(3, SECONDS);
                    service.addItem(UUID.randomUUID()).invoke().toCompletableFuture()

                    assertTrue("items list should be empty" , basketView.getItems()!=null &&  basketView.getItems().isEmpty());
                });
    }
*/


}




