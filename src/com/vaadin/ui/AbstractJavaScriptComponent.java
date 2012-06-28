/* 
@VaadinApache2LicenseForJavaFiles@
 */
package com.vaadin.ui;

import com.vaadin.terminal.JavaScriptCallbackHelper;
import com.vaadin.terminal.gwt.client.ui.JavaScriptComponentState;
import com.vaadin.terminal.gwt.client.ui.JavaScriptWidget;

/**
 * Base class for Components with all client-side logic implemented using
 * JavaScript.
 * <p>
 * When a new JavaScript component is initialized in the browser, the framework
 * will look for a globally defined JavaScript function that will initialize the
 * component. The name of the initialization function is formed by replacing .
 * with _ in the name of the server-side class. If no such function is defined,
 * each super class is used in turn until a match is found. The framework will
 * thus first attempt with <code>com_example_MyComponent</code> for the
 * server-side
 * <code>com.example.MyComponent extends AbstractJavaScriptComponent</code>
 * class. If MyComponent instead extends <code>com.example.SuperComponent</code>
 * , then <code>com_example_SuperComponent</code> will also be attempted if
 * <code>com_example_MyComponent</code> has not been defined.
 * <p>
 * JavaScript components have a very simple GWT widget ({@link JavaScriptWidget}
 * ) just consisting of a <code>div</code> element to which the JavaScript code
 * should initialize its own user interface.
 * <p>
 * The initialization function will be called with <code>this</code> pointing to
 * a connector wrapper object providing integration to Vaadin with the following
 * functions:
 * <ul>
 * <li><code>getConnectorId()</code> - returns a string with the id of the
 * connector.</li>
 * <li><code>getParentId([connectorId])</code> - returns a string with the id of
 * the connector's parent. If <code>connectorId</code> is provided, the id of
 * the parent of the corresponding connector with the passed id is returned
 * instead.</li>
 * <li><code>getElement([connectorId])</code> - returns the DOM Element that is
 * the root of a connector's widget. <code>null</code> is returned if the
 * connector can not be found or if the connector doesn't have a widget. If
 * <code>connectorId</code> is not provided, the connector id of the current
 * connector will be used.</li>
 * <li><code>getState()</code> - returns an object corresponding to the shared
 * state defined on the server. The scheme for conversion between Java and
 * JavaScript types is described bellow.</li>
 * <li><code>registerRpc([name, ] rpcObject)</code> - registers the
 * <code>rpcObject</code> as a RPC handler. <code>rpcObject</code> should be an
 * object with field containing functions for all eligible RPC functions. If
 * <code>name</code> is provided, the RPC handler will only used for RPC calls
 * for the RPC interface with the same fully qualified Java name. If no
 * <code>name</code> is provided, the RPC handler will be used for all incoming
 * RPC invocations where the RPC method name is defined as a function field in
 * the handler. The scheme for conversion between Java types in the RPC
 * interface definition and the JavaScript values passed as arguments to the
 * handler functions is described bellow.</li>
 * <li><code>getRpcProxy([name])</code> - returns an RPC proxy object. If
 * <code>name</code> is provided, the proxy object will contain functions for
 * all methods in the RPC interface with the same fully qualified name, provided
 * a RPC handler has been registered by the server-side code. If no
 * <code>name</code> is provided, the returned RPC proxy object will contain
 * functions for all methods in all RPC interfaces registered for the connector
 * on the server. If the same method name is present in multiple registered RPC
 * interfaces, the corresponding function in the RPC proxy object will throw an
 * exception when called. The scheme for conversion between Java types in the
 * RPC interface and the JavaScript values that should be passed to the
 * functions is described bellow.</li>
 * </ul>
 * The connector wrapper also supports these special functions:
 * <ul>
 * <li><code>onStateChange</code> - If the JavaScript code assigns a function to
 * the field, that function is called whenever the contents of the shared state
 * is changed.</li>
 * <li>Any field name corresponding to a call to
 * {@link #registerCallback(String, JavaScriptCallback)} on the server will
 * automatically be present as a function that triggers the registered callback
 * on the server.</li>
 * <li>Any field name referred to using
 * {@link #invokeCallback(String, Object...)} on the server will be called if a
 * function has been assigned to the field.</li>
 * </ul>
 * <p>
 * 
 * Values in the Shared State and in RPC calls are converted between Java and
 * JavaScript using the following conventions:
 * <ul>
 * <li>Primitive Java numbers (byte, char, int, long, float, double) and their
 * boxed types (Byte, Character, Integer, Long, Float, Double) are represented
 * by JavaScript numbers.</li>
 * <li>The primitive Java boolean and the boxed Boolean are represented by
 * JavaScript booleans.</li>
 * <li>Java Strings are represented by JavaScript strings.</li>
 * <li>List, Set and all arrays in Java are represented by JavaScript arrays.</li>
 * <li>Map<String, ?> in Java is represented by JavaScript object with fields
 * corresponding to the map keys.</li>
 * <li>Any other Java Map is represented by a JavaScript array containing two
 * arrays, the first contains the keys and the second contains the values in the
 * same order.</li>
 * <li>A Java Bean is represented by a JavaScript object with fields
 * corresponding to the bean's properties.</li>
 * <li>A Java Connector is represented by a JavaScript string containing the
 * connector's id.</li>
 * <li>A pluggable serialization mechanism is provided for types not described
 * here. Please refer to the documentation for specific types for serialization
 * information.</li>
 * </ul>
 * 
 * @author Vaadin Ltd
 * @version @VERSION@
 * @since 7.0.0
 */
public abstract class AbstractJavaScriptComponent extends AbstractComponent {
    private JavaScriptCallbackHelper callbackHelper = new JavaScriptCallbackHelper(
            this);

    @Override
    protected <T> void registerRpc(T implementation, Class<T> rpcInterfaceType) {
        super.registerRpc(implementation, rpcInterfaceType);
        callbackHelper.registerRpc(rpcInterfaceType);
    }

    /**
     * Register a {@link JavaScriptCallback} that can be called from the
     * JavaScript using the provided name. A JavaScript function with the
     * provided name will be added to the connector wrapper object (initially
     * available as <code>this</code>). Calling that JavaScript function will
     * cause the call method in the registered {@link JavaScriptCallback} to be
     * invoked with the same arguments.
     * 
     * @param functionName
     *            the name that should be used for client-side callback
     * @param javaScriptCallback
     *            the callback object that will be invoked when the JavaScript
     *            function is called
     */
    protected void registerCallback(String functionName,
            JavaScriptCallback javaScriptCallback) {
        callbackHelper.registerCallback(functionName, javaScriptCallback);
    }

    /**
     * Invoke a named function that the connector JavaScript has added to the
     * JavaScript connector wrapper object. The arguments should only contain
     * data types that can be represented in JavaScript, including primitive
     * boxing types, arrays, String, List, Set, Map, Connector and JavaBeans.
     * 
     * @param name
     *            the name of the function
     * @param arguments
     *            function arguments
     */
    protected void invokeCallback(String name, Object... arguments) {
        callbackHelper.invokeCallback(name, arguments);
    }

    @Override
    public JavaScriptComponentState getState() {
        return (JavaScriptComponentState) super.getState();
    }
}