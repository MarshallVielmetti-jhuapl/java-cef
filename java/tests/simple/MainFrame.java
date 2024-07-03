// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package tests.simple;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefAppHandlerAdapter;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * This is a simple example that uses the persistent callback feature.
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = -5570653778104813836L;
    private final CefApp cefApp_;
    private final CefClient client_;
    private final CefBrowser browser_;
    private final Component browserUI_;

    /**
     * To display a simple browser window, it suffices completely to create an
     * instance of the class CefBrowser and to assign its UI component to your
     * application (e.g. to your content pane).
     * But to be more verbose, this CTOR keeps an instance of each object on the
     * way to the browser UI.
     */
    private MainFrame(String startURL, boolean useOSR, boolean isTransparent) {
        // (1) The entry point to JCEF is always the class CefApp. There is only one
        // instance per application and therefore you have to call the method
        // "getInstance()" instead of a CTOR.
        //
        // CefApp is responsible for the global CEF context. It loads all
        // required native libraries, initializes CEF accordingly, starts a
        // background task to handle CEF's message loop and takes care of
        // shutting down CEF after disposing it.
        CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
            @Override
            public void stateHasChanged(org.cef.CefApp.CefAppState state) {
                // Shutdown the app if the native CEF part is terminated
                if (state == CefAppState.TERMINATED)
                    System.exit(0);
            }
        });
        CefSettings settings = new CefSettings();
        settings.windowless_rendering_enabled = useOSR;
        cefApp_ = CefApp.getInstance(settings);

        // (2) JCEF can handle one to many browser instances simultaneous. These
        // browser instances are logically grouped together by an instance of
        // the class CefClient. In your application you can create one to many
        // instances of CefClient with one to many CefBrowser instances per
        // client. To get an instance of CefClient you have to use the method
        // "createClient()" of your CefApp instance. Calling an CTOR of
        // CefClient is not supported.
        //
        // CefClient is a connector to all possible events which come from the
        // CefBrowser instances. Those events could be simple things like the
        // change of the browser title or more complex ones like context menu
        // events. By assigning handlers to CefClient you can control the
        // behavior of the browser. See tests.detailed.MainFrame for an example
        // of how to use these handlers.
        client_ = cefApp_.createClient();

        CefMessageRouter router = CefMessageRouter.create();
        router.addHandler(new CefMessageRouterHandlerAdapter() {
            private boolean hasRegisteredAlready = false;
            private boolean cancelThread = false;
            private Thread persistentThread = null;

            @Override
            public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent,
                    CefQueryCallback callback) {

                if (request.equals("persistent") && !this.hasRegisteredAlready) {
                    System.out.println("registering new persistent callback");
                    this.createPersistentCallbackThread(callback);
                    return true;
                } else if (request.equals("cancel") && this.hasRegisteredAlready) {
                    System.out.println("Cancelling thread");
                    this.cancelPersistentCallbackThread();
                    return true;
                }

                return false;
            }

            private void createPersistentCallbackThread(CefQueryCallback callback) {
                this.persistentThread = new Thread(() -> {
                    while (!this.cancelThread) {
                        try {
                            System.out.println("Attempting to send message via callback");
                            callback.success("persisting!");
                            Thread.sleep(2000);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    callback.failure(100, "cancelling persistent callback -- should be more graceful");
                    return;
                });
                this.persistentThread.start();
                return;
            }

            private void cancelPersistentCallbackThread() {
                this.cancelThread = true;
                try {
                    persistentThread.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }, true);

        client_.addMessageRouter(router);

        // (3) One CefBrowser instance is responsible to control what you'll see on
        // the UI component of the instance. It can be displayed off-screen
        // rendered or windowed rendered. To get an instance of CefBrowser you
        // have to call the method "createBrowser()" of your CefClient
        // instances.
        //
        // CefBrowser has methods like "goBack()", "goForward()", "loadURL()",
        // and many more which are used to control the behavior of the displayed
        // content. The UI is held within a UI-Compontent which can be accessed
        // by calling the method "getUIComponent()" on the instance of CefBrowser.
        // The UI component is inherited from a java.awt.Component and therefore
        // it can be embedded into any AWT UI.
        browser_ = client_.createBrowser(startURL, useOSR, isTransparent);
        browserUI_ = browser_.getUIComponent();

        // Clear focus from the address field when the browser gains focus.
        getContentPane().add(browserUI_, BorderLayout.CENTER);
        // pack();
        setSize(800, 600);
        setVisible(true);

        // (6) To take care of shutting down CEF accordingly, it's important to call
        // the method "dispose()" of the CefApp instance if the Java
        // application will be closed. Otherwise you'll get asserts from CEF.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CefApp.getInstance().dispose();
                dispose();
            }
        });
    }

    public static void main(String[] args) {
        // Perform startup initialization on platforms that require it.
        if (!CefApp.startup(args)) {
            System.out.println("Startup initialization failed!");
            return;
        }

        // The simple example application is created as anonymous class and points
        // to Google as the very first loaded page. Windowed rendering mode is used by
        // default. If you want to test OSR mode set |useOsr| to true and recompile.
        boolean useOsr = false;
        new MainFrame(
                "file:///home/marshallvielmetti/repos/java-cef/java/tests/simple/persistent_demo.html",
                useOsr,
                false);
    }
}
