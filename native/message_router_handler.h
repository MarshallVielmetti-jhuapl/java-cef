// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

#ifndef JCEF_NATIVE_MESSAGE_ROUTER_HANDLER_H_
#define JCEF_NATIVE_MESSAGE_ROUTER_HANDLER_H_
#pragma once

#include <jni.h>
#include <set>

#include "include/wrapper/cef_message_router.h"

#include "jni_scoped_helpers.h"

// MessageRouterHandler implementation.
class MessageRouterHandler : public CefMessageRouterBrowserSide::Handler,
                             public CefBaseRefCounted {


  class JcefCallback : public CefMessageRouterBrowserSide::Callback {
    public:
    JcefCallback(CefRefPtr<Callback> callback, int64_t query_id, bool persistent) : cefCallback__{callback}, jcefQuery_id__{query_id}, jcefPersistent__{persistent} {}

    void Success(const CefString& response) {
      this->cefCallback__.get()->Success(response);
    }

    void Success(const void* data, size_t size) {
      this->cefCallback__.get()->Success(data, size);
    }

    void Failure(int error_code, const CefString& error_message) {
      this->cefCallback__.get()->Failure(error_code, error_message);
    }

    ~JcefCallback() {
      this->cefCallback__.get()->~Callback();
    }

    // void Detach() {
    //   this->cefCallback__.get()->Detach();
    // }

    void AddRef() const override {
      this->cefCallback__.get()->AddRef();
    }

    bool Release() const override {
      return this->cefCallback__.get()->Release();
    }

    bool HasOneRef() const override {
      return this->cefCallback__.get()->HasOneRef();
    }

    bool HasAtLeastOneRef() const override {
      return this->cefCallback__.get()->HasAtLeastOneRef();
    }

    //These are all public for better or for worse
    const CefRefPtr<Callback> cefCallback__;
    const int64_t jcefQuery_id__;
    const bool jcefPersistent__;
  };
  
 public:
  //Maintains a global set of all query_ids associated with persistent queries
  MessageRouterHandler(JNIEnv* env, jobject handler);

  // CefMessageRouterHandler methods
  virtual bool OnQuery(CefRefPtr<CefBrowser> browser,
                       CefRefPtr<CefFrame> frame,
                       int64_t query_id,
                       const CefString& request,
                       bool persistent,
                       CefRefPtr<Callback> callback) override;
  virtual void OnQueryCanceled(CefRefPtr<CefBrowser> browser,
                               CefRefPtr<CefFrame> frame,
                               int64_t query_id) override;

 protected:
  ScopedJNIObjectGlobal handle_;

  // Include the default reference counting implementation.
  IMPLEMENT_REFCOUNTING(MessageRouterHandler);
};

#endif  // JCEF_NATIVE_MESSAGE_ROUTER_HANDLER_H_
