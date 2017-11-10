# Contributing to Bonsai

> This will need fleshing out once I think the initial release is "done" - @Olical 10th November 2017

Right now, due to the early stages of this project, I'd recomend you shoot me an email or tweet ([@OliverCaldwell][twoot]) asking about what you wish to do. Or maybe open an issue so we can discuss it.

Once I have a clear vision set out and some more features I'll be able to refer you to some sort of "this is how I think things should be" guidance document.

Even then, I'd recommend you run what you're thinking past me so we can talk about it before you do anything I have a reason to disagree with. I don't want to waste your time at all, valued contributor. :smile:

## Tooling

As far as the actual development goes, I start up a Clojure REPL within this directory (usually through CIDER jack in), then I run `(cljs-repl)` inside there. That function is defined in `dev/user.clj`, it is loaded automatically.

You should now have a functioning ClojureScript REPL that you can control with your editor. Hopefully.

Testing is performed with `lein test` for the Clojure parts and `lein test-cljs` for the ClojureScript parts. `test-cljs` maps to `lein doo node test once`. Finally, `lein test-all` will run the tests through Clojure and ClojureScript (obviously stuff like DOM testing is only run in the ClojureScript environment).

I usually have a small terminal open with `lein test-cljs-auto` (`lein doo node test auto`) running. It'll run the tests as I change the files which is very convinient.

Make sure you add some sort of test to prove your change works as expected, they will also  be run for you in [Travis CI][travis].The test results will show up on your pull request.

## Don't be shy

If you think you want to help out, or just have some questions, please feel free to contact me. I'll be more than happy to help you out or answer your questions.

Thanks for reading.

Oliver Caldwell (@Olical)

[twoot]: https://twitter.com/OliverCaldwell
[travis]: https://travis-ci.org/Olical/bonsai
