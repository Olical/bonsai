# bonsai [![Build Status](https://travis-ci.org/Olical/bonsai.svg?branch=master)](https://travis-ci.org/Olical/bonsai) [![Clojars Project](https://img.shields.io/clojars/v/olical/bonsai.svg)](https://clojars.org/olical/bonsai)

Minimalistic state management.

Designed to work well with Reagent applications, supports both ClojureScript and Clojure.

Inspired by Elm, Reagent, Redux et al.

## Usage

First add the current latest version (as indicated by the Clojars badge above) to your `project.clj`. Then require `bonsai.core` as whatever you want, I'd recommend `bonsai` or just `b`, I'll use `bonsai` here.

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
(defn calc-pi [next! res-act]
  (future                  ;; Drop into another thread.
    (Thread/sleep 1000)    ;; Wait for one second.
    (next! res-act 3.14))) ;; Pass the result onto the result handler action.

;; Define an action that can add things to :val.
(defn add [state n]
  (-> state
      (update :val (partial + n))))

;; Notice we still haven't used anything from bonsai yet?
;; It's a pretty tiny library, most of what you do is plain ol' Clojure(Script).
;; Now let's define an action that uses the calc-pi effect.
;; It'll tell calc-pi to add it to the state when it's done.
(defn add-pi [state]
  (-> state
      (bonsai/with-effect calc-pi add)))

;; Now let's apply some actions!
;; We do that by asking bonsai to advance the state! to the next state! using an action.
(bonsai/next! !state add 5)

;; 5 was added to the state.
@state ;; {:val 5}

;; Actions with effects will have their effects applied.
;; We can use calc-pi which gives pi to an action we specify.
;; In this case, we ask calc-pi to give pi to add.
(bonsai/next! !state add-pi)

;; 3.14 was added to the state.
@state ;; {:val 8.14}
```

## Unlicenced

Find the full [unlicense][] in the `UNLICENSE` file, but here's a snippet.

>This is free and unencumbered software released into the public domain.
>
>Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software, either in source code form or as a compiled binary, for any purpose, commercial or non-commercial, and by any means.

Do what you want. Learn as much as you can. Unlicense more software.

[unlicense]: http://unlicense.org/
