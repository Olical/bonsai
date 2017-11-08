> This is a work in progress and is probably just going to be a research project, not intended for full production use. If it's really useful maybe I'll keep going until it _is_ production ready though. - @Olical 8th November 2017

# Bonsai [![Build Status](https://travis-ci.org/Olical/bonsai.svg?branch=master)](https://travis-ci.org/Olical/bonsai) [![Clojars Project](https://img.shields.io/clojars/v/olical/bonsai.svg)](https://clojars.org/olical/bonsai)

Declarative DOM rendering with integrated state management for [ClojureScript][].

## To do

 * [x] Rendering basic trees of nodes and text.
 * [x] Handling of weird trees, like nested seqs of nodes and nils all over the place.
 * [x] Performing minimal changes to transform any tree into another tree.
 * [x] Adding, updating or removing node attributes such as `id` or `class` (yes, `class`, you don't need `className` here).
 * [ ] Rendering functions in the tree, these are components I guess? I make no distinction.
 * [ ] Special casing event attributes to give us some hook into events.
 * [ ] Integrating state management into the rendering and event pipeline.
 * [ ] Rendering on the server and picking up where you left off on the client. This is called "hydration" and should be easy.
 * [ ] Various optimisations for things like reordering items without changing them.
 * [ ] The inevitable bug fixes that will be required because of Internet Explorer.
 * [ ] Documenting and speccing the shit out of everything because it should be solid by this point.

## Potential usage

This is just an outline of what I want usage to be like. This is not a final design, just some syntactic doodles.

```clojure
(defn inc-value [state]
  (update state :value inc))

(defn app [state]
  [:p
    {:on-click [inc-value]}
    (str (:value state))])
```

```clojure
(ns myapp.frontend
  (:require [bonsai.dom :as dom]))
  
(dom/mount! {:value 0} [app] (js/document.getElementById "app"))
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
