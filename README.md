# bonsai [![Build Status](https://travis-ci.org/Olical/bonsai.svg?branch=master)](https://travis-ci.org/Olical/bonsai) [![Clojars Project](https://img.shields.io/clojars/v/olical/bonsai.svg)](https://clojars.org/olical/bonsai)

Minimalistic state management for [Clojure][] and [ClojureScript][].

Designed to work well with [Reagent][] applications or anything that requires state changing over time. Usually in response to some kind of user interaction or asynchronous task.

## Rationale

When working with Reagent applications (or anything that requires state changing over time) you'll find yourself updating an atom in response to something happening. In JavaScript, you'd probably lean towards [Redux][], in ClojureScript you may lean towards [re-frame][reframe].

I quite like both of these systems, but I wanted something minimal that complimented Reagent (hence the similarities in description). I wrote a few functions to encapsulate the bare minimum of what I wanted to manage in an application and Bonsai fell out of that.

The name is fairly obviously a reference to [Elm][], but this is a tiny tree that is deliberately kept small. Despite it's size, it resembles a larger version of itself. I feel like it's a good fit.

So the "architecture", if you can call it that, consists of the following.

 * An atom to store you state in. This can be a Reagent atom.
 * Functions to map the state inside the atom onto the next state, called actions.
 * Functions to perform asynchronous operations like hitting the network, called effects.
 * A function to attach effects to the state inside an action for later execution.
 * A function to apply arguments to the action and execute any effects.
 
These five simple concepts yield quite a lot of power, as I hope you'll see.

## Usage

First add the current latest version of `olical/bonsai` (as indicated by the Clojars badge above) to your `project.clj`. Then require `bonsai.core` `:as` whatever you want, I'd recommend `bonsai` or just `b`, I'll use `bonsai` here.

```clojure
;; Create your application state atom, it can contain whatever you want.
;; This could be a Reagent ratom too, as long as it can be dereferenced and swapped.
;; If you want to use effects you can not use a primative type such as a number.
;; This is because primatives can not have meta, which effects require.
(def state! (atom {:val 0}))

;; Define an effect that "calculates" Pi after a little while.
;; It passes the result onto the given action.
;; Effects get given a partially applied "next!" fn to pass values onto further actions.
;; This is a Clojure example (Thread/sleep etc), but it works exactly the same in ClojureScript.
(defn calc-pi [next! result-action]
  (future                        ;; Drop into another thread.
    (Thread/sleep 1000)          ;; Wait for one second.
    (next! result-action 3.14))) ;; Pass the result onto the result handler action.

;; Define an action that can add things to :val.
(defn add [state n]
  (-> state
      (update :val + n)))

;; Notice we still haven't used anything from bonsai yet?
;; It's a pretty tiny library, most of what you do is plain ol' Clojure(Script).
;; Now let's define an action that uses the calc-pi effect.
;; It'll tell calc-pi to add it to the state when it's done.
(defn add-pi [state]
  (-> state
      (bonsai/with-effect calc-pi add)))

;; Now let's apply some actions!
;; We do that by asking bonsai to advance the state! to the next state! using an action.
(bonsai/next! state! add 5)

;; 5 was added to the state.
(println @state!) ;; {:val 5}

;; Actions with effects will have their effects applied.
;; We can use calc-pi which gives pi to an action we specify.
;; In this case, we ask calc-pi to give pi to add.
(bonsai/next! state! add-pi)

;; Sleep until the effect is complete.
(Thread/sleep 1500)

;; 3.14 was added to the state after a little while.
(println @state!) ;; {:val 8.14}
```

And here's a slightly more practical Reagent example that you can find within the `example/` directory.

```clojure
(ns example.core
  (:require [reagent.core :as reagent]
            [bonsai.core :as bonsai]
            [clojure.string :as str]))

;; Define your state atom, in this case, a Reagent atom.
(defonce state! (reagent/atom {:text "This reverses text!"}))

(defn text-changed
  "Action that updates the text within the state."
  [state text]
  (-> state
      (assoc :text text)))

(defn reverser
  "Component that renders the current text reversed. Dispatches changes to text-changed."
  [state!]
  (let [{:keys [text]} @state!]
    [:div
     [:input {:type "text"
              :value text
              :on-change #(bonsai/next! state! text-changed (-> % .-target .-value))}]
     [:p (str/reverse text)]]))

(defn root
  "Component at the top of our app, notice how we always pass state! through."
  [state!]
  [reverser state!])

(defn init!
  "Starts up the application and mounts it into the DOM."
  []
  (reagent/render [root state!] (.getElementById js/document "app")))
```

## Inspiration

Inspired by [Elm][], [Reagent][], [Redux][], [re-frame][] et al.

## Unlicenced

Find the full [unlicense][] in the `UNLICENSE` file, but here's a snippet.

>This is free and unencumbered software released into the public domain.
>
>Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software, either in source code form or as a compiled binary, for any purpose, commercial or non-commercial, and by any means.

Do what you want. Learn as much as you can. Unlicense more software.

[clojure]: https://clojure.org/
[clojurescript]: https://clojurescript.org/
[reagent]: https://reagent-project.github.io/
[redux]: http://redux.js.org/docs/introduction/
[reframe]: https://github.com/Day8/re-frame
[elm]: http://elm-lang.org/
[unlicense]: http://unlicense.org/
