package ru.ashirobokov.qbit.todo;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.stats.StatsCollector;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.reactor.Reactor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TodoManagerTestInvokePromiseByWebSocket {
    public final static Logger LOG = LoggerFactory.getLogger(TodoManagerTestInvokePromiseByWebSocket.class);

    /** Service Server */
    private ServiceEndpointServer serviceEndpointServer;
    /** Object address to the todoManagerImpl service actor. */
    private final String todoAddress = "todo";
    /** Client service proxy to the todoManager */
    private TodoManager todoManager;
    /** QBit WebSocket Client */
    private Client webSocketClient;

    @Before
    public void setup() {

        final EndpointServerBuilder endpointServerBuilder = new EndpointServerBuilder();
        /* Create the service server. */
        serviceEndpointServer = endpointServerBuilder
                .setHost("127.0.0.1")
                .setPort(8080)
                .setHealthService(null)
                .setEnableHealthEndpoint(false)
                .build();

        /** Create service bundle . */
//        final ServiceBundleBuilder serviceBundleBuilder = serviceBundleBuilder();
//        serviceBundleBuilder.getRequestQueueBuilder().setBatchSize(1);
//        final ServiceBundle serviceBundle = serviceBundleBuilder.build();

        final TodoManagerImpl todoManagerImpl = new TodoManagerImpl(Reactor.reactor(), new StatsCollector() {
        });

        serviceEndpointServer.serviceBundle()
                .addServiceObject(todoAddress, todoManagerImpl)
                .startServiceBundle();


//        final TodoManager todoManager = serviceEndpointServer.serviceBundle().createLocalProxy(TodoManager.class, "todo");

        serviceEndpointServer.startServerAndWait();
        LOG.debug("testManager.setup SERVER ... OK ... ");

        final ClientBuilder clientBuilder = ClientBuilder.clientBuilder();

        /** Build the webSocketClient. */
        webSocketClient = clientBuilder
                .setHost("127.0.0.1")
                .setPort(8080)
                .build();
//                .setAutoFlush(true)
//                .setFlushInterval(1)
//                .setProtocolBatchSize(100)
//                .build().startClient();

        /* Create a REMOTE proxy proxy to communicate with the service actor. */
//        TodoManager todoManager = webSocketClient.createProxy(TodoManager.class, todoAddress);
        todoManager = webSocketClient.createProxy(TodoManager.class, "todo");

        /* Start the remote proxy. */
        webSocketClient.start();
        LOG.debug("testManager.setup CLIENT ... OK ... ");

        Sys.sleep(1000);

    }

    @Test
    public void test() throws Exception {

        /** Add a Todo. */
        final Promise<Boolean> addPromise = todoManager
                .add(new Todo("Buy Tesla", "Buy new Tesla", System.currentTimeMillis()))
                .then(res -> System.out.println("RESULT OF add Todo Tesla : " +res.booleanValue()))
                .catchError(Throwable::printStackTrace);
        addPromise.invoke();

        final Promise<Boolean> addPromise2 = todoManager
                .add(new Todo("Buy Something", "Buy new Something", System.currentTimeMillis()))
                .then(res -> System.out.println("RESULT OF add Todo Something : " +res.booleanValue()))
                .catchError(Throwable::printStackTrace);
        addPromise2.invoke();

//        Consumer<List<Todo>> todosConsumer = new Consumer<List<Todo>>() {
//            @Override
//            public void accept(List<Todo> todos) {
//                String id = todos.get(0).getId();
//                LOG.debug(".... LIST GET DATA ID for 0 ...{}...", id);
//                removeTodo(id, todoManager);
//            }
//        };

//  Get the list of _Todos_ into List<Todo> todos
//
        List<Todo> todos = new ArrayList<>();
        Promise<List<Todo>> listPromise = todoManager.list()
                .then(list -> list.forEach(todo -> {todos.add(todo);}));
//                .then(todosConsumer);
        listPromise.invoke();

//        Promise<String> stringPromise = todoManager.print()
//                .then(s -> System.out.println("TODOS to String :" + s));
//        stringPromise.invoke();

        LOG.debug(".... Waiting for first 10 sec .... ");
        Sys.sleep(10000);
        LOG.debug(".... 10 sec expired!.... ");

        LOG.debug("todos size {}", todos.size());

//        /** Get the id of the Todo to remove it. */
//        final String id = todos.get(0).getId();
//        LOG.debug(".... List get data id for 0 ...{}...", id);
//
//        /** Remove the todo with the todo id.  */
//        final Promise<Boolean> removePromise = todoManager
//                .remove(id)
//                .then(res -> System.out.println("RESULT OF remove of Todo Something : " +res.booleanValue()))
//                .catchError(Throwable::printStackTrace);
//        removePromise.invoke();
//
////  Get the renewed list of _Todos_ into List<Todo> todos2
////
//        List<Todo> todos2 = new ArrayList<>();
//        Promise<List<Todo>> listPromise2 = todoManager.list()
//                .then(list -> list.forEach(todo -> {todos2.add(todo);}));
//        listPromise2.invoke();
//
///** Waiting for a couple of sec to let all async opeations to complete */
//        LOG.debug(".... Waiting 10 sec .... ");
//        Sys.sleep(10000);
//        LOG.debug(".... 10 sec expired!.... ");
//
///** Print both lists for analysis */
//        assertEquals(2, todos.size());
//        LOG.debug("TODOS SIZE  {}", todos.size());
//        todos.forEach(t -> {
//            LOG.debug("Name ... {} ....", t.getName());
//        });
//
//        assertEquals(1, todos2.size());
//        LOG.debug("TODOS2 SIZE {}", todos2.size());
//        todos2.forEach(t -> {
//            LOG.debug("Name ... {} ....", t.getName());
//        });

    }

    @After
    public void tearDown() throws Exception{
        Thread.sleep(100);
        LOG.info("tearDown works now ...");
        serviceEndpointServer.stop();
        webSocketClient.stop();
    }

/*
    private void removeTodo(String id, TodoManager todoManager) {
        LOG.debug("....removeTodo....started");
        final Promise<Boolean> removePromise = todoManager
                .remove(id)
                .then(res -> System.out.println("RESULT OF remove of Todo Something : " +res.booleanValue()))
                .catchError(Throwable::printStackTrace);
        removePromise.invoke();
        LOG.debug("/....removeTodo....");
    }
*/

}
