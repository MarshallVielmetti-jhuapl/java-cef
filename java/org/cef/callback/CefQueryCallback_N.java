// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package org.cef.callback;

class CefQueryCallback_N extends CefNativeAdapter implements CefQueryCallback {
    private boolean persistent_ = false;

    CefQueryCallback_N() {
    }
    // CefQueryCallback_N(boolean persistent) {
    // System.out.println("Setting persistent to be " + persistent);
    // this.persistent_ = persistent;
    // }

    @Override
    protected void finalize() throws Throwable {
        failure(-1, "Unexpected call to CefQueryCallback_N::finalize()");
        super.finalize();
    }

    @Override
    public void success(String response) {
        try {
            N_Success(getNativeRef(null), response);
        } catch (UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void failure(int error_code, String error_message) {
        try {
            N_Failure(getNativeRef(null), error_code, error_message);
        } catch (UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    private void makePersistent() {
        System.out.println("Setting Persistent to True");
        this.persistent_ = true;
    }

    private boolean getIsPersistent() {
        return this.persistent_;
    }

    private final native void N_Success(long self, String response);

    private final native void N_Failure(long self, int error_code, String error_message);
}
