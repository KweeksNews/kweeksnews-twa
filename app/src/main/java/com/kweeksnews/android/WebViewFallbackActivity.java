// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.kweeksnews.android;

import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

public class WebViewFallbackActivity extends AppCompatActivity {
    public static final String KEY_LAUNCH_URI =
            "com.kweeksnews.com.WebViewFallbackActivity.KEY_LAUNCH_URL";

    private Uri mLaunchUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mLaunchUrl = this.getIntent().getParcelableExtra(KEY_LAUNCH_URI);

        WebView webView = new WebView(this);
        webView.setWebViewClient(createWebViewClient());

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);


        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        setContentView(webView,layoutParams);
        webView.loadUrl(mLaunchUrl.toString());
    }

    private WebViewClient createWebViewClient() {
        return new WebViewClient() {
            @Override
            public boolean onRenderProcessGone(
                    WebView view, RenderProcessGoneDetail detail) {
                ViewGroup vg = (ViewGroup)view.getParent();

                // Remove crashed WebView from the hierarchy
                // and ensure it is destroyed.
                vg.removeView(view);
                view.destroy();

                // Create a new instance, and ensure it also
                // handles crashes - in this case, re-using
                // the current WebViewClient
                WebView webView = new WebView(view.getContext());
                webView.setWebViewClient(this);
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                vg.addView(webView);

                // With the crash recovered, decide what to do next.
                // We are sending a toast and loading the origin
                // URL, in this example.
                Toast.makeText(view.getContext(), "Recovering from crash",
                        Toast.LENGTH_LONG).show();
                webView.loadUrl(mLaunchUrl.toString());
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri launchUrl = WebViewFallbackActivity.this.mLaunchUrl;
                Uri navigationUrl = request.getUrl();

                // If the user is navigation to a different origin, use CCT to handle the navigation
                if (!launchUrl.getScheme().equals(navigationUrl.getScheme())
                    || !launchUrl.getHost().equals(navigationUrl.getHost())) {
                    CustomTabsIntent intent  = new CustomTabsIntent.Builder().build();
                    intent.launchUrl(WebViewFallbackActivity.this, navigationUrl);
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, request);
            }
        };
    }
}
