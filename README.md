# bonsai [![Build Status](https://travis-ci.org/Olical/bonsai.svg?branch=master)](https://travis-ci.org/Olical/bonsai) [![Clojars Project](https://img.shields.io/clojars/v/olical/bonsai.svg)](https://clojars.org/olical/bonsai)

Minimalistic state management for [Clojure][] and [ClojureScript][].

## Rationale

When working with [Reagent][] applications (or anything that requires state changing over time) you'll find yourself updating an atom in response to something happening. In JavaScript, you'd probably lean towards [Redux][] to manage this, in ClojureScript you may lean towards [re-frame][].

I quite like both of these systems, but I wanted something minimal that complimented Reagent. The "architecture", if you can call it that, consists of the following:

 * An atom to store you state in. This can be a Reagent atom.
 * Functions to map the state inside the atom onto the next state, called actions.
 * Functions to perform asynchronous operations like hitting the network, called effects.
 * A function to attach effects to the state inside an action for later execution.
 * A function to apply arguments to the action and execute any effects.
 
These five simple concepts yield quite a lot of power, as I hope you'll see.

The name is fairly obviously a reference to [Elm][], but this is a tiny tree that is deliberately kept small. Despite it's size, it resembles a larger version of itself. I feel like it's a good fit.

## Example

Let's build a component that reverses some text input.

```clojure
;; 1. Include whatever namespaces you need.
(ns reverser.core
  (:require [reagent.core :as reagent]
            [bonsai.core :as bonsai]
            [clojure.string :as str]))

;; 2. Define an action that'll update the :text within our state with more text.
(defn text-changed
  "An action that updates the text within the state."
  [state text]
  (assoc state :text text))

;; 3. Define a component that renders the text reversed and notifies us of changes.
(defn reverser
  "An input with it's content reversed rendered below it."
  [state!]
  (let [{:keys [text]} @state!]
    [:div
     [:input {:type "text"
              :value text
              :on-change #(bonsai/next! state! text-changed (-> % .-target .-value))}]
     [:p (str/reverse text)]]))

;; 4. Define a root component that renders our reverser.
(defn root
  "Entry into the application."
  [state!]
  [reverser state!])

;; 5. Mount our application and pass it an initial state atom.
(defn init!
  "Mount the application into the page with an initial state atom."
  []
  (let [state! (reagent/atom {:text "Hello, Bonsai!"})]
    (reagent/render [root state!] (.getElementById js/document "app"))))
```

If you need to do something that isn't a pure map over the state, you can use an effect like so.

```clojure
;; 1. Define our effect.
;; Notice it is given a partially applied next! as an argument. It is already linked to our state!
;; Our :on-complete handler is a function that takes a result argument, we pass it onto the handler action we were given.
(defn submit-comment
  "An effect that submits a comment and let's you know when it's done."
  [next! comment on-complete]
  (post-comment-to-server!
    {:on-complete #(next! on-complete %)}))

;; 2. Define an action to handle the response.
(defn comment-submitted
  "Action that handles a submit response."
  [state response]
  (assoc state :response-msg (:msg response)))

;; 3. Define an action that uses our effect.
(defn submit-clicked
  "An action that starts the comment effect."
  [state comment]
  ;; 4. Attatch our effect to the state, this function is still pure.
  (bonsai/with-effect state submit-comment comment comment-submitted))

;; 5. Trigger the previous action from some button.
[:button {:on-click #(bonsai/next! state! submit-clicked current-comment)}
  "Submit"]
```

If you don't want to use anonymous functions all over the place for performance reasons, you can always do something like this.

```clojure
;; 1. Define a memoized function that returns a partially applied function.
;; That function reference is exactly the same if the arguments are the same.
(def handle (memoize partial))

;; 2. Use it to define your handlers instead of #(...).
[:button {:on-click (handle bonsai/next! state! submit-clicked current-comment)}
  "Submit"]
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
[re-frame]: https://github.com/Day8/re-frame
[elm]: http://elm-lang.org/
[unlicense]: http://unlicense.org/
