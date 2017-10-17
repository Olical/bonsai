# Bonsai [![Build Status](https://travis-ci.org/Olical/bonsai.svg?branch=master)](https://travis-ci.org/Olical/bonsai) [![Clojars Project](https://img.shields.io/clojars/v/olical/bonsai.svg)](https://clojars.org/olical/bonsai)

Declarative DOM rendering with integrated state management for [ClojureScript][].

## To do

 * [ ] `bonsai.tree` - Tools to work with hiccup like trees. Mostly just to generate change sets between them.
 * [ ] `bonsai.dom` - Mount and render trees into the DOM using `bonsai.tree` for change detection.
 * [ ] `bonsai.html` - Render a tree to HTML. The ClojureScript should be able to continue where the Clojure left off through hydration.
 * [ ] Full documentation of all public functions including examples.
 * [ ] Implement my tiny todo app with this instead of Reagent.
 * [ ] Implement another Game of Life using this instead of re-frame.

## Potential usage

This is just an outline of what I want usage to be like.

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

I'll write this up when I'm done, maybe as a blog post.

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
