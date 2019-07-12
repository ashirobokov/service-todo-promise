package ru.ashirobokov.qbit.todo;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.reactor.Reactor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TodoManagerTestBlockPromiseByWebSocket {

    public final static Logger LOG = LoggerFactory.getLogger(TodoManagerTestBlockPromiseByWebSocket.class);


    @Test
    public void testManager() throws Exception {

        final EndpointServerBuilder endpointServerBuilder = new EndpointServerBuilder();


        /* Create the service server. */
        ServiceEndpointServer serviceEndpointServer = endpointServerBuilder
                .setHost("127.0.0.1")
                .setPort(8080)
                .setHealthService(null)
                .setEnableHealthEndpoint(false)
                .build();

        /** Create service bundle . */
//        final ServiceBundleBuilder serviceBundleBuilder = serviceBundleBuilder();
//        serviceBundleBuilder.getRequestQueueBuilder().setBatchSize(1);
//        final ServiceBundle serviceBundle = serviceBundleBuilder.build();

        final TodoManagerImpl todoManagerImpl = new TodoManagerImpl(Reactor.reactor(), new StatsCollector() {});

        serviceEndpointServer.serviceBundle()
                .addServiceObject("todo", todoManagerImpl)
                .startServiceBundle();


//        final TodoManager todoManager = serviceEndpointServer.serviceBundle().createLocalProxy(TodoManager.class, "todo");

        serviceEndpointServer.startServerAndWait();
        LOG.debug("testManager.setup SERVER ... OK ... ");

        final ClientBuilder clientBuilder = ClientBuilder.clientBuilder();

        /** Build the webSocketClient. */
        Client webSocketClient = clientBuilder
                .setHost("127.0.0.1")
                .setPort(8080)
//                .setAutoFlush(true)
//                .setFlushInterval(1)
//                .setProtocolBatchSize(100)
                .build().startClient();

        /* Create a REMOTE proxy proxy to communicate with the service actor. */
        TodoManager todoManager = webSocketClient.createProxy(TodoManager.class, "todo");

        /* Start the remote proxy. */
        webSocketClient.start();
        LOG.debug("testManager.setup CLIENT ... OK ... ");

        Sys.sleep(1000);

        /** Add a Todo. */
        final Promise<Boolean> addPromise = todoManager
                .add(new Todo("Buy Tesla", "Buy new Tesla", System.currentTimeMillis()))
                .catchError(Throwable::printStackTrace);
        assertTrue(addPromise.blockingGet());

        final Promise<Boolean> addPromise2 = todoManager
                .add(new Todo("Buy Something", "Buy new Something", System.currentTimeMillis()))
                .catchError(Throwable::printStackTrace);
        assertTrue(addPromise2.blockingGet());

// Tryed non blocking Promise
//        List<Todo> todos = new ArrayList<>();
//        Promise<List<Todo>> listPromise = todoManager.list()
//                .then(list -> list.forEach(todo -> {todos.add(todo);}));
//        listPromise.invoke();
//
//        Sys.sleep(20000);
//
//        LOG.debug("SIZE  {}", todos.size());
//        todos.forEach(t -> {
//            LOG.debug("Name ... {} ....", t.getName());
//        });

        /** Call list to get a list of Todos. */
        final Promise<List<Todo>> listPromise = todoManager
                .list()
                .catchError(Throwable::printStackTrace);
        final List<Todo> todos = listPromise.blockingGet();

        assertEquals(2, todos.size());
        assertEquals("Buy Tesla", todos.get(1).getName());

        LOG.debug("TODOS SIZE BockingGet = {}", todos.size());
        LOG.debug("[TODOS] {}", todos.toString());

        /** Get the id of the Todo to remove it. */
        final String id = todos.get(0).getId();

        LOG.debug("List get data id for 0 ...{}...", id);

        /** Remove the todo with the todo id.  */
        final Promise<Boolean> removePromise = todoManager.remove(id);
        assertTrue(removePromise.blockingGet());

        /** See if the todo was removed.  */
        final Promise<List<Todo>> listPromise2 = todoManager.list();
        final List<Todo> todos2 = listPromise2.blockingGet();
        assertEquals(1, todos2.size());

        LOG.debug("TODOS2 size [blockingGet] = {} ", todos2.size());
        LOG.debug("[TODOS2] {}", todos2.toString());

    }

}
