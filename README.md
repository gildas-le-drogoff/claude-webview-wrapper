# Dead-simple Claude WebView (Android)

<p align="center">
  <img src="./Claude_AI_symbol.svg" alt="Claude AI logo" width="120" />
</p>

Minimal Android wrapper to load **[https://claude.ai](https://claude.ai)** inside a WebView.

## Principle

* WebView renders Claude
* External browser handles anything outside `claude.ai`

## Why

Claude does not work properly in a pure WebView (auth, redirects, security).
This approach works around those limitations.

## How it works

* Enables JavaScript + storage
* Enables third-party cookies
* Redirects non-`claude.ai` domains to Chrome

## Key code

```kotlin
override fun shouldOverrideUrlLoading(
    view: WebView,
    request: WebResourceRequest,
): Boolean {
    val host = request.url.host ?: return true
    return if (allowedDomains.none { host.endsWith(it) }) {
        startActivity(Intent(Intent.ACTION_VIEW, request.url))
        true
    } else {
        false
    }
}
```

## Important

Do not modify:

* external redirection
* third-party cookies

Otherwise it breaks.

## Usage

```kotlin
webView.loadUrl("https://claude.ai/new/")
```

## Limitations

* depends on Claude’s internal behavior
* WebView ≠ full browser

## License

Free to use.
