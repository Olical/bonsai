# bonsai changes

## v2.0.0

 * Various README and docstring improvements.
 * Lein test aliases.
 * Renamed `next!` to `step!`.
 * Added `stepper` which is the same as `step!` but it returns a function that will perform the `step!` when called. If you call `stepper` with the same arguments, the same fn reference is returned, great for Reagent event handlers.

## v1.0.0

Initial release.
