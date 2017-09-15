# Bonsai [![Build Status](https://travis-ci.org/Olical/bonsai.svg?branch=master)](https://travis-ci.org/Olical/bonsai) [![Clojars Project](https://img.shields.io/clojars/v/olical/bonsai.svg)](https://clojars.org/olical/bonsai)

Minimal state management and rendering for [ClojureScript][].

## Example

This is just an outline of what I want it to be like.

```clojure
(defn inc-value [state]
  (update state :value inc))

(defn app [state]
  [:p
    {:on-click [inc-value]}
    (:value state)])
```

```clojure
(ns myapp.frontend
  (:require [bonsai.dom :as dom]))
  
(dom/render [app] {:value 0}
            (js/document.getElementById "app"))
```

```clojure
(ns myapp.backend
  (:require [bonsai.html :as html]))
  
(html/render [app] {:value 0})
```

## Rationale

...

## Inspiration

Inspired by [Elm][], [Reagent][], [Redux][], [re-frame][] et al.

## Unlicenced

Find the full [unlicense][] in the `UNLICENSE` file, but here's a snippet.

>This is free and unencumbered software released into the public domain.
>
>Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software, either in source code form or as a compiled binary, for any purpose, commercial or non-commercial, and by any means.

Do what you want. Learn as much as you can. Unlicense more software.

[clojurescript]: https://clojurescript.org/
[reagent]: https://reagent-project.github.io/
[redux]: http://redux.js.org/docs/introduction/
[re-frame]: https://github.com/Day8/re-frame
[elm]: http://elm-lang.org/
[unlicense]: http://unlicense.org/
