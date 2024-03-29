package ru.ashirobokov.qbit.todo;

import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.reactive.Callback;

import java.util.List;

import static io.advantageous.qbit.annotation.QueueCallbackType.EMPTY;
import static io.advantageous.qbit.annotation.QueueCallbackType.IDLE;
import static io.advantageous.qbit.annotation.QueueCallbackType.LIMIT;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

    /**
     * <code>
     *     curl -X POST -H "Content-Type: application/json" /
     *     http://localhost:8888/v1/todo-service/todo /
     *     -d '{"id":"id1", "name":"buy tesla", "description":"daddy wants"}'
     * </code>
     *
     * <code>
     *     curl http://localhost:8888/v1/todo-service/todo
     * </code>
     */
@RequestMapping("/todo-service")
public class TodoService {

        private final TodoManager todoManager;

        public TodoService(final TodoManager todoManager) {
            this.todoManager = todoManager;
        }


        @RequestMapping(value = "/todo", method = RequestMethod.POST)
        public void add(final Callback<Boolean> callback,
                        final Todo todo) {
            todoManager.add(todo)
                    .catchError(error -> {callback.reject("Todo doesn't added to a list");})
                    .then(callback::resolve)
                    .invoke();
        }



        @RequestMapping(value = "/todo", method = RequestMethod.DELETE)
        public void remove(final Callback<Boolean> callback,
                           final @RequestParam("id") String id) {
            todoManager.remove(id)
                    .catchError(callback::reject)
                    .then(callback::resolve)
                    .invoke();
        }



        @RequestMapping(value = "/todo", method = RequestMethod.GET)
        public void list(final Callback<List<Todo>> callback) {
            todoManager.list()
                    .catchError(callback::reject)
                    .then(callback::resolve)
                    .invoke();
        }


        @RequestMapping(value = "/todo/count", method = RequestMethod.GET)
        public void size(final Callback<Integer> callback) {
            todoManager.size()
                    .catchError(callback::reject)
                    .then(callback::resolve)
                    .invoke();
        }


        @RequestMapping(value = "/todo/print", method = RequestMethod.GET)
        public void print(final Callback<String> callback) {
            todoManager.print()
                    .catchError(callback::reject)
                    .then(callback::accept)
                    .invoke();
        }


        @QueueCallback({EMPTY, IDLE, LIMIT})
        public void process() {
            flushServiceProxy(todoManager);
        }

}
